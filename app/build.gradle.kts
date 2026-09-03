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
    implementation(project(":ui:swing"))
}

application {
    mainClass.set("io.github.hideyukimori.neneclock.app.NeNeClockApplication")
}
