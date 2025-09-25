import dev.deftu.gradle.utils.includeOrShade

plugins {
    java
    kotlin("jvm")
    signing
    id("eu.kakde.gradle.sonatype-maven-central-publisher") version "1.0.6"
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
}

version = "${mcData.version}+${rootProject.properties["mod.version"]}"
apply(from = file("../../deploying/secrets.gradle.kts"))

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

toolkitLoomHelper {
    useMixinRefMap(modData.id)
}

dependencies {
    modImplementation("net.fabricmc.fabric-api:fabric-api:${mcData.dependencies.fabric.fabricApiVersion}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${mcData.dependencies.fabric.fabricLanguageKotlinVersion}")
    modImplementation(includeOrShade("org.lwjgl:lwjgl-nanovg:3.3.3")!!)

    listOf("windows", "linux", "macos", "macos-arm64").forEach { v ->
        modImplementation(includeOrShade("org.lwjgl:lwjgl-nanovg:3.3.3:natives-$v")!!)
    }
}

sonatypeCentralPublishExtension {
    groupId.set("xyz.meowing")
    artifactId.set("vexel")
    version.set(project.version as String)
    componentType = "kotlin"
    publishingType = "USER_MANAGED"
    username.set(project.findProperty("sonatypeUsername") as String? ?: "")
    password.set(project.findProperty("sonatypePassword") as String? ?: "")

    pom {
        name = "Vexel"
        description = "A simple declarative rendering library built with lwjgl's NanoVG Renderer"
        url = "https://github.com/meowing-xyz/vexel"

        licenses {
            license {
                name = "GNU General Public License v3.0"
                url = "https://www.gnu.org/licenses/gpl-3.0.en.html"
            }
        }

        developers {
            developer {
                id = "aurielyn"
                name = "Aurielyn"
            }
            developer {
                id = "mrfast"
                name = "MrFast"
            }
        }

        scm {
            connection = "scm:git:https://github.com/meowing-xyz/vexel.git"
            developerConnection = "scm:git:https://github.com/meowing-xyz/vexel.git"
            url = "https://github.com/meowing-xyz/vexel"
        }
    }
}

signing {
    useGpgCmd()
}