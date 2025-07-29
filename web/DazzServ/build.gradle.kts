import kotlin.io.path.fileSize

plugins {
    // Note: the build.gradle.kts plugins block has a special syntax and cannot directly use
    // variables from gradle.properties -- specify these in settings.gradle.kts instead.
    java
    kotlin("jvm")
    application     // provides "run" task: "gradlew run --args="foo --bar"
    distribution    // provides "assembleDist" task, generates build/distribution/*.tar+zip
    id("com.github.johnrengelman.shadow")  // task "shadowJar" for single JAR
}

// Values from gradle.properties
val propArtifactVers: String by project
val propArtifactGroup: String by project
val propVersJava: String by project
val propVersJunit: String by project
val propVersTruth: String by project

group   = propArtifactGroup
version = propArtifactVers

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(propVersJava.toInt())
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

application {
    mainClass = "com.alfray.dazzserv.DazzServ"
}

dependencies {
    implementation(project(":LibManifest"))

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:$propVersJunit")
    testImplementation("com.google.truth:truth:$propVersTruth")
}

tasks.test {
    // useJUnitPlatform() is needed only for JUnit5 with Gradle. We use JUnit4 here.
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.alfray.dazzserv.DazzServ"
    }
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
