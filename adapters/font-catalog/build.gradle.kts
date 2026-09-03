import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis

plugins {
    id("neneclock.java-conventions")
}

description = "利用可能な書体の一覧を実行環境から読める唯一の場所（ARC-007 の例外区画）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
}

// 🔑 このモジュールだけ platform.txt を外す。実行環境を覗ける窓口がここ 1 つであることを、
//    ビルドファイルの差分として一目で見える形に残す。時計は読めないままにするため
//    determinism.txt は外さない。
tasks.withType<CheckForbiddenApis>().configureEach {
    signaturesFiles =
        files(
            rootProject.file("config/forbiddenapis/base.txt"),
            rootProject.file("config/forbiddenapis/determinism.txt"),
        )
}
