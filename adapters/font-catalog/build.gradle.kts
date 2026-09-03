plugins {
    id("neneclock.java-conventions")
}

description = "同梱書体（Google Fonts / OFL）の実体を持つ唯一の場所（ADR 0006）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
}
