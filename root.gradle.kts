plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    "1.21.9-fabric"(1_21_09, "yarn") {
        "1.21.9-neoforge"(1_21_09, "srg") {

            "1.21.7-fabric"(1_21_07, "yarn") {
                "1.21.7-forge"(1_21_07, "srg") {
                    "1.21.7-neoforge"(1_21_07, "srg") {

                        "1.21.5-fabric"(1_21_05, "yarn") {
                            "1.21.5-neoforge"(1_21_05, "srg") {
                                "1.21.5-forge"(1_21_05, "srg") {

                                    "1.20.1-forge"(1_20_01, "srg") {
                                        "1.20.1-fabric"(1_20_01, "yarn")
                                    }

                                }
                            }
                        }

                    }
                }
            }

        }
    }
}