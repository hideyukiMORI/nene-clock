plugins {
    id("neneclock.java-conventions")
}

description = "java.util.prefs に触れる唯一の場所（ARC-002 / ARC-009）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
}
