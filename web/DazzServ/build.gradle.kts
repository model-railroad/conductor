import kotlin.io.path.fileSize

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm")
}

val vers_java: String by project
val artifact_vers: String by project
val artifact_group: String by project
val vers_junit: String by project
val vers_truth: String by project

group   = artifact_group
version = artifact_vers

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(vers_java.toInt())
}

dependencies {
    implementation(project(":LibManifest"))

    testImplementation(kotlin("test"))
    testImplementation("junit:junit:$vers_junit")
    testImplementation("com.google.truth:truth:$vers_truth")
}

tasks.test {
    useJUnitPlatform()
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
