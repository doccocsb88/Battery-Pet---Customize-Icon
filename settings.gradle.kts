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
include(":theme_pack_ocean")
include(":theme_pack_floralgarden")
include(":theme_pack_countryside")
include(":theme_pack_fantasy")
include(":theme_pack_chinese_spring_landscape")

val localStorePackDir = file("store_pack")
if (localStorePackDir.exists()) {
    project(":store_pack").projectDir = localStorePackDir
} else {
    project(":store_pack").projectDir = file("../store_pack")
}

project(":theme_pack_ocean").projectDir = file("theme_pack_ocean")
project(":theme_pack_floralgarden").projectDir = file("theme_pack_floralgarden")
project(":theme_pack_countryside").projectDir = file("theme_pack_countryside")
project(":theme_pack_fantasy").projectDir = file("theme_pack_fantasy")
project(":theme_pack_chinese_spring_landscape").projectDir = file("theme_pack_chinese_spring_landscape")
