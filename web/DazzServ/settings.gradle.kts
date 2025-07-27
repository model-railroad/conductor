pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm").version("2.1.21")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "DazzServ"

include("LibUtilsJava")
project(":LibUtilsJava").projectDir = File(rootProject.projectDir, "../../android/RTAC/LibUtils/LibUtilsJava")

include("LibManifest")
project(":LibManifest").projectDir = File(rootProject.projectDir, "../../android/RTAC/LibManifest")
