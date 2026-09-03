pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "nene-clock"

include(":core:domain")
include(":core:application")
include(":adapters:system-time")
include(":adapters:preferences")
include(":adapters:font-catalog")
include(":ui:swing")
include(":app")
include(":quality:architecture-tests")
