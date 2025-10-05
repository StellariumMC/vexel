plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    "1.21.9-fabric"(1_21_09, "yarn") {
        "1.21.7-fabric"(1_21_07, "yarn") {
            "1.21.5-fabric"(1_21_05, "yarn") {
                "1.20.1-fabric"(1_20_01, "yarn")
            }
        }
    }
}