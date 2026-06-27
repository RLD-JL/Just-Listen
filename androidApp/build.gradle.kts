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
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        applicationId = "com.rld.justlisten.android"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = libs.versions.android.versionCode.get().toInt()
        versionName = libs.versions.android.versionName.get()
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

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
                ?: gradleLocalProperties(rootDir, providers).getProperty("RELEASE_KEYSTORE_PATH")
            if (!keystorePath.isNullOrEmpty()) {
                storeFile = file(keystorePath)
                storePassword = System.getenv("RELEASE_KEYSTORE_PASSWORD")
                    ?: gradleLocalProperties(rootDir, providers).getProperty("RELEASE_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("RELEASE_KEY_ALIAS")
                    ?: gradleLocalProperties(rootDir, providers).getProperty("RELEASE_KEY_ALIAS")
                keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
                    ?: gradleLocalProperties(rootDir, providers).getProperty("RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles("proguard-rules.pro")
            
            val keystorePath = System.getenv("RELEASE_KEYSTORE_PATH")
                ?: gradleLocalProperties(rootDir, providers).getProperty("RELEASE_KEYSTORE_PATH")
            if (!keystorePath.isNullOrEmpty()) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    dependenciesInfo {
        // Disables dependency metadata when building APKs (for IzzyOnDroid/F-Droid)
        includeInApk = false
        // Disables dependency metadata when building Android App Bundles (for Google Play)
        includeInBundle = false
    }
    namespace = "com.rld.justlisten.android"
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}
