plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.android.material)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.palette)

    implementation(libs.bundles.compose)
    debugImplementation(libs.compose.ui.preview)

    implementation(libs.bundles.media3)

    implementation(libs.snapper)
    implementation(libs.androidx.work.runtime)

    implementation(libs.lifecycle.process)
    implementation(libs.coil3.compose)

    implementation(libs.androidx.core.splashscreen)
    debugImplementation(libs.leakcanary.android)
    
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
