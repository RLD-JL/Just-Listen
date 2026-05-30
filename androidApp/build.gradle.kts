plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10"
}

dependencies {
    implementation(project(":shared"))
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.preview)

    implementation(libs.bundles.media3)

    implementation(libs.snapper)
    implementation(libs.androidx.work.runtime)

    implementation("androidx.lifecycle:lifecycle-process:2.8.4")
    implementation("io.coil-kt:coil-compose:2.7.0")

    implementation("androidx.core:core-splashscreen:1.0.1")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.14")
    
    // Navigation Compose
    implementation(libs.navigation.compose)
    
    // Koin for Compose
    implementation(libs.koin.androidx.compose)
}


android {
    compileSdk = 35
    defaultConfig {
        applicationId = "com.rld.justlisten.android"
        minSdk = 21
        targetSdk = 35
        versionCode = 26
        versionName = "1.0.9-a"
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
    kotlinOptions {
        jvmTarget = "17"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
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

/*
composeCompiler {
    enableStrongSkippingMode = true

    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    stabilityConfigurationFile = rootProject.layout.projectDirectory.file("stability_config.conf")
}*/
