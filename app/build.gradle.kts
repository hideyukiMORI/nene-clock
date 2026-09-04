import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import javax.inject.Inject
import org.gradle.process.ExecOperations

plugins {
    id("neneclock.java-conventions")
    application
}

description = "合成ルート。配線と起動のみを持ち、業務判断を持たない（ARC-006）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
    implementation(project(":adapters:system-time"))
    implementation(project(":adapters:preferences"))
    implementation(project(":adapters:font-catalog"))
    implementation(project(":ui:swing"))
}

application {
    mainClass.set("io.github.hideyukimori.neneclock.app.NeNeClockApplication")
}

// 🔑 合成ルートだけが端末とプロセスの終了コードを扱ってよい（ADR 0005）。
//    窓が出る前に失敗したとき、利用者へ届く経路は端末しか無いため。
//    ほかのモジュールで同じことを書いたら forbidden-apis と ArchUnit が拒否する。
tasks.withType<CheckForbiddenApis>().configureEach {
    bundledSignatures = setOf("jdk-unsafe", "jdk-deprecated", "jdk-non-portable", "jdk-internal", "jdk-reflection")
    signaturesFiles =
        files(
            rootProject.file("config/forbiddenapis/base.txt"),
            rootProject.file("config/forbiddenapis/determinism.txt"),
            rootProject.file("config/forbiddenapis/platform.txt"),
        )
}

// 🔑 配布物のアイコンは、実装（AppIcon）から書き出す。画像をリポジトリに置かない。
//    置くと「描いている絵」と「置いた絵」が別々に存在し、片方だけ古くなる。
tasks.register<JavaExec>("writeAppIcons") {
    group = "distribution"
    description = "アプリのアイコンを PNG として書き出す（配布物を作るときに使う）"
    mainClass.set("io.github.hideyukimori.neneclock.app.AppIconFiles")
    classpath = sourceSets["main"].runtimeClasspath
    systemProperty("java.awt.headless", "true")
    args(layout.buildDirectory.dir("icons").get().asFile.path)
}

// 🔑 配布物（インストーラー）を作る経路は 1 つ（ARC-012 / QLT-005 / ADR 0013）。
//    CI の Windows ランナーもこの task を呼ぶだけで、ワークフローに手順を書かない。
//    Windows では MSI を作る。ほかの OS では同じ結線で app-image を作り、
//    jar・main クラス・アイコン・モジュール集合が正しいことを起動して証明できる。
//    モジュール集合は jdeps から機械的に求める。手で書くと、足りないときに実行時まで分からない。
abstract class PackageInstaller : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    abstract val libDirectory: DirectoryProperty

    @get:InputDirectory
    abstract val iconDirectory: DirectoryProperty

    @get:InputFile
    abstract val licenseFile: RegularFileProperty

    @get:Input
    abstract val jdkHome: Property<String>

    @get:Input
    abstract val mainClass: Property<String>

    @get:Input
    abstract val mainJarName: Property<String>

    @get:Input
    abstract val appVersion: Property<String>

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    @TaskAction
    fun run() {
        val onWindows = System.getProperty("os.name").startsWith("Windows")
        val bin = File(jdkHome.get(), "bin")
        val lib = libDirectory.get().asFile
        val jars = lib.listFiles { file -> file.name.endsWith(".jar") }!!.sorted()
        val modules = ByteArrayOutputStream().also { captured ->
            execOperations.exec {
                executable = File(bin, "jdeps").path
                args("--print-module-deps", "--ignore-missing-deps", "--multi-release", "21", "--class-path", File(lib, "*").path)
                args(jars.map { it.path })
                standardOutput = captured
            }
        }.toString(Charsets.UTF_8).trim()
        val out = destination.get().asFile
        out.deleteRecursively()
        out.mkdirs()
        val icon = File(iconDirectory.get().asFile, if (onWindows) "nene-clock.ico" else "nene-clock-256.png")
        execOperations.exec {
            executable = File(bin, "jpackage").path
            args("--type", if (onWindows) "msi" else "app-image")
            args("--name", "NeNe Clock")
            args("--app-version", appVersion.get())
            args("--vendor", "hideyukiMORI")
            args("--description", "A quiet desktop clock")
            args("--input", lib.path)
            args("--main-jar", mainJarName.get())
            args("--main-class", mainClass.get())
            args("--icon", icon.path)
            args("--add-modules", modules)
            args("--dest", out.path)
            if (onWindows) {
                args("--license-file", licenseFile.get().asFile.path)
                args("--win-menu", "--win-shortcut", "--win-per-user-install")
                // 入れ直しを「上書き」にする鍵。変えると別製品として並んで入る。
                args("--win-upgrade-uuid", "ea39da0b-604d-46ab-8ac1-69d155faaec8")
            }
        }
        out.listFiles { file -> file.isFile }!!.forEach { file ->
            val digest = MessageDigest.getInstance("SHA-256").digest(file.readBytes())
            val hex = digest.joinToString("") { byte -> "%02x".format(byte) }
            File(out, file.name + ".sha256").writeText("$hex  ${file.name}\n")
        }
        logger.lifecycle("modules: {}", modules)
        logger.lifecycle("installer: {}", out.listFiles()!!.joinToString { it.name })
    }
}

tasks.register<PackageInstaller>("packageInstaller") {
    group = "distribution"
    description = "インストーラーを作る（Windows は MSI、それ以外は app-image で結線を確かめる）"
    dependsOn(tasks.named("installDist"), tasks.named("writeAppIcons"))
    libDirectory.set(layout.buildDirectory.dir("install/app/lib"))
    iconDirectory.set(layout.buildDirectory.dir("icons"))
    licenseFile.set(rootProject.layout.projectDirectory.file("LICENSE"))
    jdkHome.set(javaToolchains.launcherFor(java.toolchain).map { it.metadata.installationPath.asFile.absolutePath })
    mainClass.set(application.mainClass)
    mainJarName.set(tasks.named<Jar>("jar").flatMap { it.archiveFileName })
    appVersion.set(project.version.toString())
    destination.set(layout.buildDirectory.dir("installer"))
}
