pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EmojiBatteryPort"
include(":app")
include(":store_pack")

val localStorePackDir = file("store_pack")
if (localStorePackDir.exists()) {
    project(":store_pack").projectDir = localStorePackDir
} else {
    project(":store_pack").projectDir = file("../store_pack")
}
