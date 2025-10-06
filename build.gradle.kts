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

toolkitMultiversion {
    moveBuildsToRootProject.set(true)
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    compileOnly("com.github.odtheking:odin-lwjgl:68de0d3e0b")

    api("org.lwjgl:lwjgl-nanovg:3.3.3")
    api("org.lwjgl:lwjgl-stb:3.3.3")
    api(shade("dev.deftu:isolated-lwjgl3-loader:0.3.2") {
        exclude(group = "org.apache")
        exclude(group = "org.intellij")
        exclude(group = "org.jetbrains")
    })
    modApi(shade("xyz.meowing:knit-${mcData}:102")!!)
}

toolkitMavenPublishing {
    artifactName.set("vexel")
    setupRepositories.set(false)
}

java {
    withSourcesJar()
    withJavadocJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(8)
}

afterEvaluate {
    publishing {
        publications {
            named<MavenPublication>("mavenJava") {
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