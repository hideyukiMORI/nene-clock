plugins {
    id("neneclock.java-conventions")
}

description = "アーキテクチャを実行可能にする（QLT-006）。テスト専用で production 依存を持たない"

dependencies {
    testImplementation(project(":core:domain"))
    testImplementation(project(":core:application"))
    testImplementation(project(":adapters:system-time"))
    testImplementation(project(":adapters:preferences"))
    testImplementation(project(":ui:swing"))
    testImplementation(project(":app"))
    testImplementation(libs.archunit)
}
