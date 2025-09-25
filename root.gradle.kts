plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    val fabric_1_21_5 = createNode("1.21.5-fabric", 1_21_05, "yarn")
    val fabric_1_21_7 = createNode("1.21.7-fabric", 1_21_07, "yarn")
    fabric_1_21_5.link(fabric_1_21_7)
}