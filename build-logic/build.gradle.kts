plugins {
    `kotlin-dsl`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(libs.plugins.errorprone.toDependencyNotation())
    implementation(libs.plugins.spotless.toDependencyNotation())
    implementation(libs.plugins.forbiddenapis.toDependencyNotation())
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

gradlePlugin {
    plugins {
        register("conformance") {
            id = "neneclock.conformance"
            implementationClass = "io.github.hideyukimori.neneclock.gradle.conformance.ConformancePlugin"
        }
    }
}

fun Provider<PluginDependency>.toDependencyNotation(): Provider<String> = map {
    "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version.requiredVersion}"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
