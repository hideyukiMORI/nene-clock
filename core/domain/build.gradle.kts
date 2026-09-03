plugins {
    id("neneclock.java-conventions")
}

description = "意味の正本。JDK 標準ライブラリ以外に依存しない（ARC-003）"

// ARC-003 の物理的な保証: production 依存を 1 つも持たない。
configurations.matching { it.name == "implementation" || it.name == "api" }.configureEach {
    dependencies.whenObjectAdded {
        throw GradleException(":core:domain は production 依存を持てない（ARC-003）: $this")
    }
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
