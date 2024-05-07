import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import kotlin.io.path.fileSize

/*
 * Project: Conductor
 * Copyright (C) 2017 alf.labs gmail com,
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

// ----------
// IMPORTANT: all plugin/deps VERSIONS are defined in gradle.properties.
// ----------

plugins {
    // https://plugins.gradle.org/plugin/com.github.johnrengelman.shadow
    id("com.github.johnrengelman.shadow") version "8.1.1"
    java
    idea
}

repositories {
    mavenCentral()
}

fun String.v() : Any = extra[this]!!

group   = "artifact_group".v()
version = "artifact_vers".v()


dependencies {
    implementation(project(":common"))
    implementation(project(":engine1"))
    implementation(project(":engine2k"))
    implementation(project(":ui2"))
    implementation(project(":ui2k"))
    implementation(project(":simul2k"))
    implementation(project(":LibManifest"))
    implementation(project(":LibUtilsJava"))

    implementation("com.google.guava:guava:"  + "vers_guava".v())
    implementation("com.intellij:forms_rt:"  + "vers_forms_rt".v())
    implementation("com.google.truth:truth:"  + "vers_truth".v())
    implementation("com.fasterxml.jackson.core:jackson-databind:"  + "vers_jackson".v())
    implementation("org.jmdns:jmdns:"  + "vers_jmdsn".v())
    implementation("org.slf4j:slf4j-simple:"  + "vers_sl4j".v())     // used by jmdns
    implementation("com.squareup.okhttp3:okhttp:"  + "vers_okhttp".v())
    implementation("org.codehaus.groovy:groovy-all:"  + "vers_groovy".v())

    // For Kotlin KTS and Scripting Host:
    implementation("org.jetbrains.kotlin:kotlin-stdlib:"  + "vers_kotlin".v())
    implementation("org.jetbrains.kotlin:kotlin-scripting-common:"  + "vers_kotlin".v())

    implementation(         "com.google.dagger:dagger:"  + "vers_dagger".v())
    annotationProcessor(    "com.google.dagger:dagger-compiler:"  + "vers_dagger".v())
    testAnnotationProcessor("com.google.dagger:dagger-compiler:"  + "vers_dagger".v())

    testImplementation(testFixtures(project(":common")))
    testImplementation(testFixtures(project(":engine1")))
    testImplementation("junit:junit:"  + "vers_junit".v())
    testImplementation("com.google.truth:truth:"  + "vers_truth".v())
    testImplementation("org.mockito:mockito-core:"  + "vers_mockito".v())
    testImplementation("com.google.guava:guava:"  + "vers_guava".v())
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.alflabs.conductor.EntryPoint"
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
