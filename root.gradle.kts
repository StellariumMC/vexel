plugins {
    id("dev.deftu.gradle.multiversion-root")
}

preprocess {
    "1.21.10-fabric"(1_21_10, "yarn") {

        "1.21.9-fabric"(1_21_09, "yarn") {
            "1.21.9-forge"(1_21_09, "srg") {
                "1.21.9-neoforge"(1_21_09, "srg") {

                    "1.21.8-fabric"(1_21_08, "yarn") {
                        "1.21.8-forge"(1_21_08, "srg") {
                            "1.21.8-neoforge"(1_21_08, "srg") {

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

    }
}