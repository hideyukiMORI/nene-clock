import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Properties
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
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

// 🔑 版の正本は gradle.properties の version。product.properties へ埋めて、実行時はそこから読む（#82）。
//    コードに版を書くと 2 か所になり、片方だけ上がる。
tasks.named<ProcessResources>("processResources") {
    filesMatching("**/product.properties") {
        expand("version" to project.version.toString())
    }
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

    @get:InputDirectory
    abstract val debResources: DirectoryProperty

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

    /** 出来たばかりの 1 つの配布物を、版を含まない決まった名前へ改める。名前は README が直に指す。 */
    private fun renameTo(directory: File, suffix: String, wanted: String) {
        directory.listFiles { file -> file.name.endsWith(suffix) && file.name != wanted }?.forEach { file ->
            if (!file.renameTo(File(directory, wanted))) {
                throw GradleException("could not rename ${file.name} to $wanted")
            }
        }
    }

    /** フォルダを zip に畳む。entry 名はフォルダ名から始める（展開すると同じ名前のフォルダが 1 つできる）。 */
    private fun zipDirectory(directory: File, target: File) {
        ZipOutputStream(target.outputStream().buffered()).use { zip ->
            directory.walkTopDown().filter { it.isFile }.sortedBy { it.path }.forEach { file ->
                val name = directory.name + "/" + file.relativeTo(directory).path.replace(File.separatorChar, '/')
                zip.putNextEntry(ZipEntry(name))
                file.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

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
        // 1. app-image（exe ＋ 実行環境のフォルダ）。すべての OS で作る。
        val images = File(out, "image")
        execOperations.exec {
            executable = File(bin, "jpackage").path
            args("--type", "app-image")
            args("--name", "NeNe Clock")
            args("--app-version", appVersion.get())
            args("--vendor", "hideyukiMORI")
            args("--description", "A quiet desktop clock")
            args("--input", lib.path)
            args("--main-jar", mainJarName.get())
            args("--main-class", mainClass.get())
            args("--icon", icon.path)
            args("--add-modules", modules)
            args("--dest", images.path)
        }
        val image = File(images, "NeNe Clock")
        // 2. ポータブル zip。展開して exe を押すだけで動く。何も入れない（ADR 0014）。
        val platform = if (onWindows) "windows" else System.getProperty("os.name").lowercase().replace(" ", "-")
        // 🔴 ファイル名に空白を入れない。GitHub は Release の添付名の空白をドットに変える（v0.2.0 で実測・#72）。
        //    展開後のフォルダ名と exe 名（"NeNe Clock"）は製品名なので、そちらは変えない。
        // 🔴 名前に**版を入れない**。README から releases/latest/download/<名前> で直に指すためで、
        //    版を入れると新しい版を出すたびに README のリンクが切れる（#88）。
        //    版は MSI と .deb のメタデータに入り、zip 版も設定モーダルのフッターに出る（#82）。
        zipDirectory(image, File(out, "NeNe-Clock-$platform-portable.zip"))
        // 3. MSI は同じ app-image から作る（Windows だけ）。結線は 1 と同じものを使う。
        if (onWindows) {
            execOperations.exec {
                executable = File(bin, "jpackage").path
                args("--type", "msi")
                args("--app-image", image.path)
                args("--name", "NeNe Clock")
                args("--app-version", appVersion.get())
                args("--vendor", "hideyukiMORI")
                args("--license-file", licenseFile.get().asFile.path)
                args("--win-menu", "--win-shortcut", "--win-per-user-install")
                // 入れ直しを「上書き」にする鍵。変えると別製品として並んで入る。
                args("--win-upgrade-uuid", "ea39da0b-604d-46ab-8ac1-69d155faaec8")
                args("--dest", out.path)
            }
            renameTo(out, ".msi", "NeNe-Clock-Setup.msi")
        }
        // 4. .deb も同じ app-image から作る（Linux だけ・ADR 0015）。/opt/nene-clock に入り、メニューに出て、apt remove で消える。
        //    dpkg-deb と fakeroot が要る。無ければ jpackage が「どの道具か」を言って落ちる。
        if (System.getProperty("os.name").startsWith("Linux")) {
            execOperations.exec {
                executable = File(bin, "jpackage").path
                args("--type", "deb")
                args("--app-image", image.path)
                args("--name", "NeNe Clock")
                args("--app-version", appVersion.get())
                args("--vendor", "hideyukiMORI")
                args("--license-file", licenseFile.get().asFile.path)
                args("--linux-package-name", "nene-clock")
                args("--linux-deb-maintainer", "info@ayane.co.jp")
                args("--linux-menu-group", "Utility")
                args("--linux-app-category", "utils")
                args("--linux-shortcut")
                // 🔴 --app-image から作るときも --icon を渡す。渡さないと jpackage は app-image の中の
                //    アイコンを流用せず、**自前の既定（duke）**をデスクトップ項目に使う。
                //    施主の Ubuntu 実機で「アイコンが既定のまま」になっていた原因がこれだった（#84）。
                args("--icon", File(iconDirectory.get().asFile, "nene-clock-256.png").path)
                // postinst と .desktop を差し替える（app/src/deb/）。最小構成の Linux でメニュー登録が落ちないようにし、
                // GNOME が動いている窓とメニュー項目を結びつけられるように StartupWMClass を書くため。
                args("--resource-dir", debResources.get().asFile.path)
                args("--dest", out.path)
            }
            renameTo(out, ".deb", "nene-clock_amd64.deb")
        }
        // 🔑 できた .deb を開いて、デスクトップ項目が指すアイコンが**我々の絵**であることを確かめる。
        //    jpackage は --icon を渡さないと自前の既定（duke）を黙って使う。それに 2 版ぶん気づけなかった（#84）。
        //    「渡したつもり」を人の記憶に頼らせない。
        File(out, "").listFiles { file -> file.name.endsWith(".deb") }?.forEach { deb ->
            val opened = File(temporaryDir, "deb-check").also { it.deleteRecursively() }
            execOperations.exec {
                executable = "dpkg-deb"
                args("-x", deb.path, opened.path)
            }
            val entry = opened.walkTopDown().first { it.name.endsWith(".desktop") }
            val iconPath = entry.readLines().first { it.startsWith("Icon=") }.removePrefix("Icon=")
            val packaged = File(opened, iconPath.removePrefix("/"))
            val wanted = File(iconDirectory.get().asFile, "nene-clock-256.png")
            if (!packaged.exists() || !packaged.readBytes().contentEquals(wanted.readBytes())) {
                throw GradleException(
                    "the .deb desktop entry points at an icon that is not ours: $iconPath " +
                        "(jpackage falls back to its own default when --icon is missing)")
            }
            opened.deleteRecursively()
        }
        out.listFiles { file -> file.isFile && !file.name.endsWith(".sha256") }!!.forEach { file ->
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
    description = "配布物を作る（app-image → ポータブル zip → Windows なら MSI、Linux なら .deb）"
    dependsOn(tasks.named("installDist"), tasks.named("writeAppIcons"))
    libDirectory.set(layout.buildDirectory.dir("install/app/lib"))
    iconDirectory.set(layout.buildDirectory.dir("icons"))
    licenseFile.set(rootProject.layout.projectDirectory.file("LICENSE"))
    debResources.set(layout.projectDirectory.dir("src/deb"))
    jdkHome.set(javaToolchains.launcherFor(java.toolchain).map { it.metadata.installationPath.asFile.absolutePath })
    mainClass.set(application.mainClass)
    mainJarName.set(tasks.named<Jar>("jar").flatMap { it.archiveFileName })
    appVersion.set(project.version.toString())
    destination.set(layout.buildDirectory.dir("installer"))
}

// 🔑 Microsoft Store へ出すパッケージ（MSIX）は、packageInstaller が作った app-image を**そのまま**包む（ADR 0020）。
//    MSIX のために jar も実行環境も作り直さない。zip・MSI・MSIX は同じ中身から出る。
//    署名はしない（Store が署名する）。鍵も証明書も持たない。
//    makeappx は Windows SDK の道具なので、Windows でだけ作る（MSI と同じ扱い）。
abstract class PackageMsix : DefaultTask() {
    @get:Inject
    abstract val execOperations: ExecOperations

    @get:InputDirectory
    abstract val imageDirectory: DirectoryProperty

    @get:InputDirectory
    abstract val logoDirectory: DirectoryProperty

    @get:InputFile
    abstract val identityFile: RegularFileProperty

    @get:Input
    abstract val appVersion: Property<String>

    @get:Input
    abstract val sdkBinDirectory: Property<String>

    @get:OutputDirectory
    abstract val destination: DirectoryProperty

    private fun escape(text: String): String =
        text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")

    private fun sha256(file: File): String =
        MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { byte -> "%02x".format(byte) }

    /** makeappx は 1 ファイルごとに 1 行を出す。成功したら黙らせ、失敗したら全部見せる。 */
    private fun makeappx(tool: File, vararg arguments: String) {
        val captured = ByteArrayOutputStream()
        val result =
            execOperations.exec {
                executable = tool.path
                args(arguments.toList())
                standardOutput = captured
                errorOutput = captured
                isIgnoreExitValue = true
            }
        if (result.exitValue != 0) {
            throw GradleException("makeappx ${arguments.first()} failed (${result.exitValue}):\n$captured")
        }
    }

    @TaskAction
    fun run() {
        val out = destination.get().asFile
        out.deleteRecursively()
        out.mkdirs()
        if (!System.getProperty("os.name").startsWith("Windows")) {
            logger.lifecycle("msix: skipped (makeappx is a Windows SDK tool; the MSIX is built on Windows only)")
            return
        }
        // 🔴 道具は SDK の版のフォルダを名指しする。ランナーでは PATH に無い（2026-10-04 実測）。
        //    SDK が変わったら黙って別の版を使わず、ここで落ちる。
        val tool = File(sdkBinDirectory.get(), "makeappx.exe")
        if (!tool.isFile) {
            throw GradleException("makeappx.exe is not where it is pinned: ${tool.path}")
        }
        val version = appVersion.get()
        if (!Regex("""\d+\.\d+\.\d+""").matches(version)) {
            throw GradleException("the version must be three numbers to become an MSIX version: $version")
        }
        val identity = Properties().also { loaded -> identityFile.get().asFile.inputStream().use { loaded.load(it) } }
        val image = imageDirectory.get().asFile
        val layout = File(temporaryDir, "layout").also { it.deleteRecursively() }
        if (!image.copyRecursively(layout)) {
            throw GradleException("could not copy the app-image to ${layout.path}")
        }
        val assets = File(layout, "Assets").also { it.mkdirs() }
        val logos = listOf("StoreLogo", "Square44x44Logo", "Square150x150Logo")
        logos.forEach { name -> File(logoDirectory.get().asFile, "$name.png").copyTo(File(assets, "$name.png")) }
        // Store は 4 番目を自分で使うので 0 にする。先頭は 0 にできない（Store が拒否する）。
        File(layout, "AppxManifest.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <Package xmlns="http://schemas.microsoft.com/appx/manifest/foundation/windows10"
                     xmlns:uap="http://schemas.microsoft.com/appx/manifest/uap/windows10"
                     xmlns:rescap="http://schemas.microsoft.com/appx/manifest/foundation/windows10/restrictedcapabilities"
                     IgnorableNamespaces="uap rescap">
              <Identity Name="${escape(identity.getProperty("identityName"))}"
                        Publisher="${escape(identity.getProperty("publisher"))}"
                        Version="$version.0"
                        ProcessorArchitecture="x64" />
              <Properties>
                <DisplayName>NeNe Clock</DisplayName>
                <PublisherDisplayName>${escape(identity.getProperty("publisherDisplayName"))}</PublisherDisplayName>
                <Logo>Assets\StoreLogo.png</Logo>
              </Properties>
              <Dependencies>
                <TargetDeviceFamily Name="Windows.Desktop" MinVersion="10.0.19041.0" MaxVersionTested="10.0.26100.0" />
              </Dependencies>
              <Resources>
                <Resource Language="en-us" />
                <Resource Language="ja-jp" />
              </Resources>
              <Applications>
                <Application Id="NeNeClock" Executable="NeNe Clock.exe" EntryPoint="Windows.FullTrustApplication">
                  <uap:VisualElements DisplayName="NeNe Clock"
                                      Description="A quiet desktop clock"
                                      BackgroundColor="transparent"
                                      Square150x150Logo="Assets\Square150x150Logo.png"
                                      Square44x44Logo="Assets\Square44x44Logo.png" />
                </Application>
              </Applications>
              <Capabilities>
                <rescap:Capability Name="runFullTrust" />
              </Capabilities>
            </Package>
            """.trimIndent() + "\n",
            Charsets.UTF_8,
        )
        // 🔴 先頭が 0 の版は Store に出せない。作れはするので（手元で入れて確かめられる）、名前で分かるようにする。
        val submittable = !version.startsWith("0.")
        val msix = File(out, if (submittable) "NeNe-Clock-store.msix" else "NeNe-Clock-store-NOT-SUBMITTABLE.msix")
        makeappx(tool, "pack", "/o", "/d", layout.path, "/p", msix.path)
        // 🔑 包んだものを開き直して、app-image の全ファイルが同じ中身で入っていることを確かめる。
        //    「同じ app-image から出る」を人の記憶に頼らせない。
        val opened = File(temporaryDir, "verify").also { it.deleteRecursively() }
        makeappx(tool, "unpack", "/o", "/p", msix.path, "/d", opened.path)
        val shipped = image.walkTopDown().filter { it.isFile }.toList()
        shipped.forEach { file ->
            val packaged = File(opened, file.relativeTo(image).path)
            if (!packaged.isFile || sha256(packaged) != sha256(file)) {
                throw GradleException("the MSIX does not carry the app-image file unchanged: ${file.relativeTo(image).path}")
            }
        }
        opened.deleteRecursively()
        File(out, msix.name + ".sha256").writeText("${sha256(msix)}  ${msix.name}\n")
        if (!submittable) {
            logger.warn("msix: version $version starts with 0. It installs locally, but Microsoft Store rejects it.")
        }
        logger.lifecycle("msix: {} ({} app-image files verified, version {}.0)", msix.name, shipped.size, version)
    }
}

tasks.register<PackageMsix>("packageMsix") {
    group = "distribution"
    description = "Microsoft Store 用の MSIX を作る（packageInstaller の app-image をそのまま包む。Windows だけ）"
    dependsOn(tasks.named("packageInstaller"))
    imageDirectory.set(layout.buildDirectory.dir("installer/image/NeNe Clock"))
    logoDirectory.set(layout.buildDirectory.dir("icons/msix"))
    identityFile.set(layout.projectDirectory.file("src/msix/store-identity.properties"))
    appVersion.set(project.version.toString())
    sdkBinDirectory.set("""C:\Program Files (x86)\Windows Kits\10\bin\10.0.26100.0\x64""")
    destination.set(layout.buildDirectory.dir("msix"))
}
