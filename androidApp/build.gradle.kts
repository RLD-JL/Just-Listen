plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    kotlin("kapt")
    id("dagger.hilt.android.plugin")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.6.1")
    implementation("androidx.appcompat:appcompat:1.4.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.0-alpha01")
    implementation("androidx.activity:activity-compose:1.5.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.preview)

    implementation (libs.bundles.exoplayer)

    implementation ("dev.chrisbanes.snapper:snapper:0.2.2")
    implementation ("androidx.work:work-runtime-ktx:2.7.1")


    implementation("androidx.lifecycle:lifecycle-process:2.6.0-alpha01")
    implementation("io.coil-kt:coil-compose:2.1.0")
    implementation("com.google.dagger:hilt-android:2.42")
    kapt("com.google.dagger:hilt-android-compiler:2.42")

    implementation("com.google.accompanist:accompanist-swiperefresh:0.24.1-alpha")
    implementation("androidx.core:core-splashscreen:1.0.0-rc01")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.7")
}

kapt {
    correctErrorTypes = true
}

android {
    compileSdk = 33
    defaultConfig {
        applicationId = "com.rld.justlisten.android"
        minSdk = 21
        targetSdk = 33
        versionCode = 22
        versionName = "1.0.7-b"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
        }
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.rld.justlisten.android"
}

sourceSets {
    android {
        kotlinOptions {
            freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
        }
    }
}