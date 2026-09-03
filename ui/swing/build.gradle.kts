plugins {
    id("neneclock.java-conventions")
}

description = "Swing 描画。状態を描き、意図を発行するだけ（ARC-011 / SWG-002）"

dependencies {
    implementation(project(":core:application"))
    implementation(project(":core:domain"))
}

// 🔑 テストだけ同梱書体の実体に触れる。文言がその言語の書体で描けるかは、
//    実体が無いと確かめられない（FR-048）。production の依存は増やさない。
dependencies {
    testImplementation(project(":adapters:font-catalog"))
}
