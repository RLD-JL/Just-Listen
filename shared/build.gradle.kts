import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

val generateBuildConfig by tasks.registering {
    val localProps = gradleLocalProperties(rootDir, providers)
    val apiKey = localProps.getProperty("AUDIUS_API_KEY") ?: System.getenv("AUDIUS_API_KEY") ?: ""
    val bearerToken = localProps.getProperty("AUDIUS_BEARER_TOKEN") ?: System.getenv("AUDIUS_BEARER_TOKEN") ?: ""
    val playstoreBuild = project.findProperty("playstoreBuild")?.toString()?.toBoolean() ?: false
    val androidVersionName = libs.versions.android.versionName.get()
    val outputDir = layout.buildDirectory.dir("generated/buildconfig/commonMain/kotlin")
    
    inputs.property("playstoreBuild", playstoreBuild)
    inputs.property("apiKey", apiKey)
    inputs.property("bearerToken", bearerToken)
    inputs.property("androidVersionName", androidVersionName)
    outputs.dir(outputDir)
    doLast {
        val outputFile = outputDir.get().file("com/rld/justlisten/BuildConfig.kt").asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText("""
            package com.rld.justlisten

            object BuildConfig {
                const val IS_PLAYSTORE_BUILD: Boolean = $playstoreBuild
                const val AUDIUS_API_KEY: String = "$apiKey"
                const val AUDIUS_BEARER_TOKEN: String = "$bearerToken"
                const val ANDROID_VERSION_NAME: String = "$androidVersionName"
            }
        """.trimIndent())
    }
}

kotlin {
    jvmToolchain(17)
    android {
        namespace = "com.rld.justlisten"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder { sourceSetTreeName = "test" }
        androidResources { enable = true }
    }



    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosSimulatorArm64

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
                isStatic = true
                linkerOpts("-lsqlite3")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateBuildConfig)
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.bundles.ktor)

                implementation(libs.jetbrains.compose.runtime)
                implementation(libs.jetbrains.compose.foundation)
                implementation(libs.jetbrains.compose.material3)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.jetbrains.compose.ui)
                implementation(libs.jetbrains.compose.resources)
                implementation(libs.jetbrains.compose.ui.tooling.preview)

                // Navigation Compose (Multiplatform)
                implementation(libs.navigation.compose)

                // ViewModel + Lifecycle
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.runtime)

                // Serialization for routes
                implementation(libs.kotlinx.serialization.json)

                // Koin DI
                implementation(libs.koin.core)
                implementation(libs.koin.compose)
                implementation(libs.koin.compose.viewmodel)

                implementation(libs.sqldelight.coroutines.extensions)

                // Image Loading (Coil 3)
                implementation(libs.coil3.compose)
                implementation(libs.coil3.network.ktor)

                // Logging
                implementation(libs.kermit)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.sqldelight.android.driver)

                implementation(libs.bundles.media3)

                implementation(libs.androidx.media)
                implementation(libs.androidx.palette)
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.activity.compose)

                // Additional Android dependencies
                implementation(libs.androidx.work.runtime)

                // Lifecycle for Android
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.runtime)
                implementation(libs.lifecycle.viewmodel.compose)

                // Koin for Android
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
            }
        }

        val iosMain by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
                implementation(libs.sqldelight.native.driver)
            }
        }
        val iosTest by getting
    }
}

sqldelight {
    databases {
        create("LocalDb") {
            packageName.set("com.rld.justlisten")
        }
    }
}




