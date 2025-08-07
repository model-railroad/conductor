import kotlin.io.path.fileSize

plugins {
    // Note: the build.gradle.kts plugins block has a special syntax and cannot directly use
    // variables from gradle.properties -- specify these in settings.gradle.kts instead.
    java
    kotlin("jvm")
    kotlin("kapt")
    application     // provides "run" task: "gradlew run --args="foo --bar"
    distribution    // provides "assembleDist" task, generates build/distribution/*.tar+zip
    id("com.github.johnrengelman.shadow")  // task "shadowJar" for single JAR
}

// Values from gradle.properties
val propArtifactVers: String by project
val propArtifactGroup: String by project
val propVersJava: String by project
val propVersDagger: String by project
val propVersClikt: String by project
val propVersJetty: String by project
val propVersSLF4J: String by project
val propVersJackson: String by project
val propVersJunit: String by project
val propVersTruth: String by project
val propVersMockitoKotlin: String by project

group   = propArtifactGroup
version = propArtifactVers

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(propVersJava.toInt())
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

application {
    mainClass = "com.alfray.dazzserv.Main"
}

dependencies {
    implementation(project(":LibUtilsJava"))
    implementation(project(":LibManifest"))
    implementation("com.github.ajalt.clikt:clikt:$propVersClikt")
    implementation("org.eclipse.jetty:jetty-server:$propVersJetty")
    implementation("org.slf4j:slf4j-simple:$propVersSLF4J")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$propVersJackson")

    implementation("com.google.dagger:dagger:$propVersDagger")
    kapt("com.google.dagger:dagger-compiler:$propVersDagger")
    kaptTest("com.google.dagger:dagger-compiler:$propVersDagger")

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:$propVersJunit")
    testImplementation("com.google.truth:truth:$propVersTruth")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$propVersMockitoKotlin")
}

tasks.test {
    // useJUnitPlatform() is needed only for JUnit5 with Gradle. We use JUnit4 here.
}

tasks.withType<Jar> {
    doFirst {
        val elems = project.configurations.runtimeClasspath.get().elements.get()
        println("## $name: Packaging ${elems.size} libraries")
        elems.forEach {
            val n = it.asFile.name
            if (n.contains("SNAPSHOT")) {
                println("## $name: adding $n")
            }
        }
    }
    doLast {
        outputs.files.forEach { file ->
            val b = file.toPath().fileSize()
            val mb = String.format("%.03f MB", b.toDouble() / 1024.0 / 1024.0)
            println("## $name: output [$mb] ${file.absolutePath}")
        }
    }
}
