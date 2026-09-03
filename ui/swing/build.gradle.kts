plugins {
    id("neneclock.java-conventions")
}

description = "Swing 描画。状態を描き、意図を発行するだけ（ARC-011 / SWG-002）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
}
