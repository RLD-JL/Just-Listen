import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
@Suppress("DSL_SCOPE_VIOLATION")

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version libs.versions.kotlinVersion.get()
    id("com.squareup.sqldelight")
}

kotlin {
    android()

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.2")
                implementation(libs.bundles.ktor)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-android:${libs.versions.ktorVersion.get()}")
                implementation("com.squareup.sqldelight:android-driver:1.5.3")

            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:${libs.versions.ktorVersion.get()}")
                implementation("com.squareup.sqldelight:native-driver:1.5.3")

            }
        }
        val iosTest by getting
    }
}

sqldelight {
    database("LocalDb") {
        packageName = "myLocal.db"
        sourceFolders = listOf("kotlin")
    }
}

android {
    compileSdk = 30
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = 21
        targetSdk = 30
    }
    namespace = "com.rld.justlisten"
}