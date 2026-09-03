import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis

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
