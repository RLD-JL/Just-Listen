import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
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

        val localProps = gradleLocalProperties(rootDir, providers)
        buildConfigField(
            "String",
            "AUDIUS_API_KEY",
            "\"${localProps.getProperty("AUDIUS_API_KEY") ?: System.getenv("AUDIUS_API_KEY") ?: ""}\""
        )
        buildConfigField(
            "String",
            "AUDIUS_BEARER_TOKEN",
            "\"${localProps.getProperty("AUDIUS_BEARER_TOKEN") ?: System.getenv("AUDIUS_BEARER_TOKEN") ?: ""}\""
        )
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true  // required for BuildConfig fields in AGP 8+
    }


    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "com.rld.justlisten.android"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}