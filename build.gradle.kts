import dev.deftu.gradle.utils.includeOrShade
import org.apache.commons.lang3.SystemUtils

plugins {
    java
    kotlin("jvm")
    id("dev.deftu.gradle.multiversion")
    id("dev.deftu.gradle.tools")
    id("dev.deftu.gradle.tools.resources")
    id("dev.deftu.gradle.tools.bloom")
    id("dev.deftu.gradle.tools.shadow")
    id("dev.deftu.gradle.tools.minecraft.loom")
    id("dev.deftu.gradle.tools.minecraft.releases")
    id("dev.deftu.gradle.tools.publishing.maven")
}

version = "${mcData.version}+${rootProject.properties["mod.version"]}"

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

toolkitLoomHelper {
    useMixinRefMap(modData.id)
}

loom {
    runConfigs {
        "client" {
            property("fml.coreMods.load", "meowing.zen.lwjgl.plugin.LWJGLLoadingPlugin")
            if (SystemUtils.IS_OS_MAC_OSX) vmArgs.remove("-XstartOnFirstThread")
        }
        remove(getByName("server"))
    }
}

dependencies {
    implementation(includeOrShade(kotlin("stdlib-jdk8"))!!)
    implementation(includeOrShade("org.jetbrains.kotlin:kotlin-reflect:1.6.10")!!)

    modImplementation(includeOrShade("com.github.odtheking:odin-lwjgl:68de0d3e0b")!!)
}

toolkitMavenPublishing {
    artifactName.set("vexel")
    setupRepositories.set(false)
}

tasks.withType<Jar> {
    manifest.attributes.run {
        this["FMLCorePlugin"] = "xyz.meowing.vexel.lwjgl.plugin.LWJGLLoadingPlugin"
    }
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            groupId = "xyz.beta"
            artifactId = "vexel-${mcData}"
            version = "113"
        }
    }
    repositories {
        mavenLocal()
    }
}