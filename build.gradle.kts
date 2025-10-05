plugins {
    idea
    java
    signing
    `maven-publish`
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "2.0.0"
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
    withSourcesJar()
    withJavadocJar()
}

loom {
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
    }
}

tasks.compileJava {
    dependsOn(tasks.processResources)
}

sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
    java.srcDir(layout.projectDirectory.dir("src/main/kotlin"))
    kotlin.destinationDirectory.set(java.destinationDirectory)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://maven.deftu.dev/snapshots")
    maven("https://maven.deftu.dev/releases")
}

val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:1.8.9")
    mappings("de.oceanlabs.mcp:mcp_stable:22-1.8.9")
    forge("net.minecraftforge:forge:1.8.9-11.15.1.2318-1.8.9")

    implementation(kotlin("stdlib-jdk8"))

    compileOnly("com.github.odtheking:odin-lwjgl:68de0d3e0b")

    api("org.lwjgl:lwjgl-nanovg:3.3.3")
    api("org.lwjgl:lwjgl-stb:3.3.3")
    shadowImpl("dev.deftu:isolated-lwjgl3-loader:0.3.2") {
        exclude(group = "org.apache")
        exclude(group = "org.intellij")
        exclude(group = "org.jetbrains")
    }
}

tasks.withType(JavaCompile::class) {
    options.encoding = "UTF-8"
}

tasks.shadowJar {
    archiveClassifier.set("dev")
    configurations = listOf(shadowImpl)
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

val remapJar by tasks.named<net.fabricmc.loom.task.RemapJarTask>("remapJar") {
    archiveClassifier.set("")
    dependsOn(tasks.shadowJar)
    from(zipTree(tasks.shadowJar.get().archiveFile))
}

tasks.jar {
    archiveClassifier.set("thin")
    destinationDirectory.set(layout.buildDirectory.dir("devlibs"))
}

tasks.assemble.get().dependsOn(tasks.remapJar)

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = "vexel-1.8.9-forge"
                groupId = project.properties["mod.group"] as String
                version = project.properties["mod.version"] as String

                artifact(tasks.remapJar.get().archiveFile)
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])

                pom {
                    name.set("Vexel")
                    description.set("A simple declarative rendering library built with lwjgl's NanoVG Renderer")
                    url.set("https://github.com/meowing-xyz/vexel")
                    licenses {
                        license {
                            name.set("GNU General Public License v3.0")
                            url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
                        }
                    }
                    developers {
                        developer {
                            id.set("aurielyn")
                            name.set("Aurielyn")
                        }
                        developer {
                            id.set("mrfast")
                            name.set("MrFast")
                        }
                    }
                    scm {
                        url.set("https://github.com/meowing-xyz/vexel")
                    }
                }
            }
        }
        repositories {
            maven {
                name = "Bundle"
                url = uri(layout.buildDirectory.dir("central-bundle"))
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}

tasks.register<Zip>("sonatypeBundle") {
    group = "publishing"
    from(layout.buildDirectory.dir("central-bundle"))
    archiveFileName.set("sonatype-bundle.zip")
    destinationDirectory.set(layout.buildDirectory)
    dependsOn("publishMavenJavaPublicationToBundleRepository")
}