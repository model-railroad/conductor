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

// TBD REMOVE IF NOT USEFUL
//    // https://stackoverflow.com/questions/75263047/duplicate-class-in-kotlin-android
//    // To find the version of kotlin, run $ ./gradlew :app:dependencies | grep kotlin
//    // Some androidx libs use 1.8.10 and some use 1.6.21, which creates duplicate conflicts.
//    constraints {
//        implementation(libs.kotlin.stdlib.jdk7) {
//            because("kotlin-stdlib-jdk7 is now a part of kotlin-stdlib")
//        }
//        implementation(libs.kotlin.stdlib.jdk8) {
//            because("kotlin-stdlib-jdk8 is now a part of kotlin-stdlib")
//        }
//    }
}
