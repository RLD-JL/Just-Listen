import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(17)
    androidTarget()
    
    sourceSets.all {
        languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
    }

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget =
        if (System.getenv("SDK_NAME")?.startsWith("iphoneos") == true)
            ::iosArm64
        else
            ::iosX64

    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
                isStatic = true
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.bundles.ktor)
                
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)

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
                
                implementation("androidx.media:media:1.7.0")
                implementation(libs.androidx.palette)
                implementation(libs.androidx.core.ktx)
                
                // Additional Android dependencies
                implementation(libs.androidx.work.runtime)
                implementation(libs.snapper)

                // Lifecycle for Android
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.runtime)
                implementation(libs.lifecycle.viewmodel.compose)
                
                // Koin for Android
                implementation(libs.koin.android)
                implementation(libs.koin.androidx.compose)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.2")
            }
        }
        val iosMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-ios:${libs.versions.ktorVersion.get()}")
                implementation("app.cash.sqldelight:native-driver:2.0.2")
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

configurations {
    named("debugFrameworkIos") {
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "my-unique-attribute"))
        }
    }
    named("releaseFrameworkIos") {
        attributes {
            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "huh"))
        }
    }
}

android {
    compileSdk = 35
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        testOptions.targetSdk = 35
        minSdk = 21
    }
    namespace = "com.rld.justlisten"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

