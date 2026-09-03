plugins {
    id("neneclock.java-conventions")
}

description = "振る舞いの調整。ポートを宣言し、実装は知らない（ARC-002）"

dependencies {
    api(project(":core:domain"))
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    violationRules {
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.90".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestCoverageVerification"))
}
