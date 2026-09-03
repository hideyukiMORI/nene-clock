import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.artifacts.VersionCatalogsExtension

plugins {
    `java-library`
    checkstyle
    jacoco
    id("net.ltgt.errorprone")
    id("com.diffplug.spotless")
    id("de.thetaphi.forbiddenapis")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

fun library(alias: String) = libs.findLibrary(alias).orElseThrow()

fun version(alias: String) = libs.findVersion(alias).orElseThrow().requiredVersion

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(version("java").toInt()))
    }
}

// QLT-002: 警告は失敗する。抑制は waiver（CNF-002）でしか存在できない。
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(version("java").toInt())
    options.compilerArgs.addAll(listOf("-Xlint:all", "-Werror", "-parameters"))
    options.errorprone {
        disableWarningsInGeneratedCode.set(true)
        allDisabledChecksAsWarnings.set(false)
        error("NullAway")
        option("NullAway:AnnotatedPackages", "io.github.hideyukimori.neneclock")
        option("NullAway:CheckOptionalEmptiness", "true")
    }
}

dependencies {
    errorprone(library("errorprone-core"))
    errorprone(library("nullaway"))
    compileOnly(library("jspecify"))
    testCompileOnly(library("jspecify"))
    testImplementation(platform(library("junit-bom")))
    testImplementation(library("junit-jupiter"))
    testImplementation(library("assertj-core"))
    testRuntimeOnly(library("junit-platform-launcher"))
}

// QLT-004: 整形は「検査」であって「CI が直すもの」ではない。正典は 1 つ。
spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat(version("palantir-java-format"))
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

checkstyle {
    toolVersion = version("checkstyle")
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    maxWarnings = 0
    maxErrors = 0
}

// テストソースだけ MagicNumber / JavadocType を外す。理由は checkstyle-test.xml の冒頭に書く。
tasks.named<Checkstyle>("checkstyleTest") {
    configFile = rootProject.file("config/checkstyle/checkstyle-test.xml")
}

// 機械強制の分担: forbidden-apis は「メソッド単位」、ArchUnit は「パッケージ／レイヤ単位」。
// 🔴 signaturesFiles をここで組み立てて main / test の両方へ渡す。片方だけに渡すと、
//    署名ファイルが存在するのに誰も読まない状態が静かに成立する（実測あり・Issue #26）。
val projectSignatures =
    files(
        rootProject.file("config/forbiddenapis/base.txt"),
        rootProject.file("config/forbiddenapis/determinism.txt"),
        rootProject.file("config/forbiddenapis/platform.txt"),
    )

forbiddenApis {
    bundledSignatures =
        setOf("jdk-unsafe", "jdk-deprecated", "jdk-non-portable", "jdk-internal", "jdk-reflection", "jdk-system-out")
    signaturesFiles = projectSignatures
    failOnUnsupportedJava = false
    ignoreSignaturesOfMissingClasses = true
}

tasks.named<de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis>("forbiddenApisTest") {
    // テストは固定時刻の生成に java.time を素で使ってよい（Clock.fixed / LocalDateTime.of）。
    // ただし実時刻を読むことは禁じる。テストが実時刻を読むのも決定性の破壊である。
    bundledSignatures = setOf("jdk-deprecated", "jdk-non-portable", "jdk-internal", "jdk-reflection")
    signaturesFiles = projectSignatures
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    // QLT-012: 単体テストは表示サーバを要求しない。
    systemProperty("java.awt.headless", "true")
    testLogging {
        events("failed")
    }
}

jacoco {
    toolVersion = "0.8.13"
}

tasks.named<Test>("test") {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required.set(true)
        html.required.set(false)
    }
}

tasks.named("check") {
    dependsOn(tasks.named("spotlessCheck"))
}

// QLT-011: 依存は再現可能。lock ファイルのドリフトはビルドを落とす。
dependencyLocking {
    lockAllConfigurations()
}
