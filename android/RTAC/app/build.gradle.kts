/*
 * Project: RTAC
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

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.alflabs.rtac"

    compileSdk = 37

    defaultConfig {
        applicationId = "com.alflabs.rtac"
        minSdk = 23
        targetSdk = 37
        versionCode = 1007
        versionName = "1.07"
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        targetCompatibility = JavaVersion.VERSION_11
        sourceCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = false
    }
}

dependencies {
    implementation(project(":LibManifest"))
    implementation(project(":LibUtilsJava"))
    implementation(project(":LibUtilsAndroid"))
    implementation(project(":androidsvg"))

    implementation(libs.jackson.databind)
    implementation(libs.okhttp)
    implementation(libs.androidx.fragment)
    implementation(libs.guava.android)

    implementation(libs.dagger)
    annotationProcessor(libs.dagger.compiler)

    // Testing: Robolectric, Mockito and Truth
    testImplementation(libs.mockito.core)
    testImplementation(libs.robolectric)
    testImplementation(libs.truth)
    testImplementation(libs.junit)
    testImplementation(libs.guava.jre)
}

tasks.withType<Test> {
    // Fix for Robolectric 4.17 issue with SDK 37.
    // see https://github.com/robolectric/robolectric/issues/11434
    jvmArgs(
        "--add-exports",
        "java.base/jdk.internal.access=ALL-UNNAMED",
    )

    // Fix: Attach Mockito as a Java Agent at test runtime. Needed for recent JVM (17+).
    jvmArgumentProviders.add(CommandLineArgumentProvider {
        val agentJar = classpath.files.find { file ->
            file.name.contains("mockito-core") || file.name.contains("byte-buddy-agent")
        }
        println("Mockito Agent runtime class path: $agentJar")
        if (agentJar != null) {
            listOf("-javaagent:${agentJar.absolutePath}")
        } else {
            emptyList()
        }
    })
}
