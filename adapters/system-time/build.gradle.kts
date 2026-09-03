import de.thetaphi.forbiddenapis.gradle.CheckForbiddenApis

plugins {
    id("neneclock.java-conventions")
}

description = "現在時刻を JDK から読める唯一の場所（ARC-007 の例外区画）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
}

// 🔑 このモジュールだけ determinism.txt を外す。ARC-007 の「唯一の窓口」がここであることを、
//    ビルドファイルの差分として一目で見える形に残す。他のモジュールで同じことを書いたら
//    validateConformance ではなく PR レビューで落とす（QLT-010）。
//
//    ⚠️ この上書きは 2026-09-03 まで no-op だった。convention 側が determinism.txt を
//    読み込んでおらず、外す対象が存在しなかったため（Issue #26）。
tasks.withType<CheckForbiddenApis>().configureEach {
    signaturesFiles =
        files(
            rootProject.file("config/forbiddenapis/base.txt"),
            rootProject.file("config/forbiddenapis/platform.txt"),
        )
}
