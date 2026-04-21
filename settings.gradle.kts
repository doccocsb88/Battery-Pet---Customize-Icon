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

val packsRootDir = file("app_pack")

val localStorePackDir = file("app_pack/store_pack")
if (localStorePackDir.exists()) {
    project(":store_pack").projectDir = localStorePackDir
} else {
    project(":store_pack").projectDir = file("../store_pack")
}

project(":theme_pack_ocean").projectDir = file("app_pack/theme_pack_ocean")
project(":theme_pack_floralgarden").projectDir = file("app_pack/theme_pack_floralgarden")
project(":theme_pack_countryside").projectDir = file("app_pack/theme_pack_countryside")
project(":theme_pack_fantasy").projectDir = file("app_pack/theme_pack_fantasy")
project(":theme_pack_chinese_spring_landscape").projectDir = file("app_pack/theme_pack_chinese_spring_landscape")

val homePackDirs = packsRootDir.listFiles()
    ?.filter { it.isDirectory && it.name.startsWith("home_pack_") }
    .orEmpty()
    .sortedBy { it.name }

homePackDirs.forEach { dir ->
    val moduleName = ":${dir.name}"
    include(moduleName)
    project(moduleName).projectDir = dir
}

val stickerPackDirs = packsRootDir.listFiles()
    ?.filter { it.isDirectory && it.name.startsWith("sticker_pack_") }
    .orEmpty()
    .sortedBy { it.name }

stickerPackDirs.forEach { dir ->
    val moduleName = ":${dir.name}"
    include(moduleName)
    project(moduleName).projectDir = dir
}

val wallpaperPackDirs = packsRootDir.listFiles()
    ?.filter { it.isDirectory && it.name.startsWith("wallpaper_pack_") }
    .orEmpty()
    .sortedBy { it.name }

wallpaperPackDirs.forEach { dir ->
    val moduleName = ":${dir.name}"
    include(moduleName)
    project(moduleName).projectDir = dir
}

include(":theme_options_pack")
project(":theme_options_pack").projectDir = file("app_pack/theme_options_pack")
