pluginManagement {
    plugins {
        // This section specifies the desired versions for the following plugins,
        // and these versions will be used when these are used in a build.gradle.kts plugins block.
        val propVersKotlin: String by settings
        val propVersShadow: String by settings
        val propVersKtSerlt: String by settings
        id("org.jetbrains.kotlin.jvm") version propVersKotlin
        id("com.github.johnrengelman.shadow") version propVersShadow
        id("org.jetbrains.kotlin.plugin.serialization") version propVersKtSerlt
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
