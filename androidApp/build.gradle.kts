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
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.0-rc01")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation("androidx.compose.ui:ui-util:1.2.0-beta03")
    implementation("androidx.compose.material:material:1.2.0-beta03")
    implementation("androidx.compose.ui:ui:1.2.0-beta03")
    implementation("androidx.compose.animation:animation:1.1.1")
    debugImplementation("androidx.compose.ui:ui-tooling:1.1.1")
    implementation("androidx.compose.ui:ui-tooling-preview:1.1.1")

    implementation ("com.google.android.exoplayer:exoplayer-core:2.17.1")
    implementation ("com.google.android.exoplayer:extension-mediasession:2.17.1")
    implementation ("com.google.android.exoplayer:exoplayer-ui:2.17.1")

    implementation("androidx.lifecycle:lifecycle-process:2.4.1")
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
    compileSdk = 32
    defaultConfig {
        applicationId = "com.rld.justlisten.android"
        minSdk = 21
        targetSdk = 32
        versionCode = 16
        versionName = "1.0.4"
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
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.2.0-beta03"
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