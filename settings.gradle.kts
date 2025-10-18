pluginManagement {
    repositories {
        // Repositories
        maven("https://maven.deftu.dev/releases")
        maven("https://maven.deftu.dev/snapshots")
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev/")
        maven("https://maven.minecraftforge.net")
        maven("https://repo.essential.gg/repository/maven-public")
        maven("https://server.bbkr.space/artifactory/libs-release/")
        maven("https://jitpack.io/")

        mavenCentral()
        mavenLocal()

        gradlePluginPortal()
    }

    plugins {
        kotlin("jvm") version("2.0.0")
        id("dev.deftu.gradle.multiversion-root") version("2.59.0")
    }
}

val projectName: String = extra["mod.name"].toString()

rootProject.name = projectName
rootProject.buildFileName = "root.gradle.kts"

listOf(
    "1.20.1-fabric",
    "1.20.1-forge",

    "1.21.5-fabric",
    "1.21.5-forge",
    "1.21.5-neoforge",

    "1.21.7-fabric",
    "1.21.7-forge",
    "1.21.7-neoforge",

    "1.21.9-fabric",
    "1.21.9-forge",
    "1.21.9-neoforge"
).forEach { version ->
    include(":$version")
    project(":$version").apply {
        projectDir = file("versions/$version")
        buildFileName = "../../build.gradle.kts"
    }
}