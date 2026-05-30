pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlinVersion", "2.1.10")
            version("coroutinesVersion", "1.10.1")

            version("ktorVersion", "3.0.3")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktorVersion")
            library("ktor-client-core", "io.ktor", "ktor-client-core").versionRef("ktorVersion")
            library("ktor-serialization-kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktorVersion")
            library("ktor-client-logging", "io.ktor", "ktor-client-logging").versionRef("ktorVersion")
            bundle(
                "ktor",
                listOf(
                    "ktor-client-core",
                    "ktor-client-content-negotiation",
                    "ktor-serialization-kotlinx-json",
                    "ktor-client-logging"
                )
            )

            version("composeVersion", "1.7.3")
            version("lifecycleVersion", "2.8.4")
            version("navigationVersion", "2.8.0-alpha10")
            
            // Compose
            library("compose-ui-util", "androidx.compose.ui", "ui-util").versionRef("composeVersion")
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("composeVersion")
            library("compose-ui-preview", "androidx.compose.ui", "ui-tooling-preview").versionRef("composeVersion")
            library("compose-material", "androidx.compose.material", "material").versionRef("composeVersion")
            library("compose-material3", "androidx.compose.material3", "material3").version("1.3.1")
            library("compose-material-icons", "androidx.compose.material", "material-icons-extended").versionRef("composeVersion")
            library("compose-animation", "androidx.compose.animation", "animation").versionRef("composeVersion")
            bundle(
                "compose",
                listOf(
                    "compose-ui",
                    "compose-ui-util",
                    "compose-ui-preview",
                    "compose-material",
                    "compose-material3",
                    "compose-material-icons",
                    "compose-animation"
                )
            )
            
            // Navigation Compose (Multiplatform)
            library("navigation-compose", "org.jetbrains.androidx.navigation", "navigation-compose").versionRef("navigationVersion")
            
            // Lifecycle & ViewModel
            library("lifecycle-viewmodel", "androidx.lifecycle", "lifecycle-viewmodel").versionRef("lifecycleVersion")
            library("lifecycle-runtime", "androidx.lifecycle", "lifecycle-runtime").versionRef("lifecycleVersion")
            library("lifecycle-viewmodel-compose", "androidx.lifecycle", "lifecycle-viewmodel-compose").versionRef("lifecycleVersion")
            
            // Serialization
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.8.0")
            
            // Testing
            library("kotlin-test", "org.jetbrains.kotlin", "kotlin-test").versionRef("kotlinVersion")
            library("kotlinx-coroutines-test", "org.jetbrains.kotlinx", "kotlinx-coroutines-test").versionRef("coroutinesVersion")

            // Koin DI
            version("koinVersion", "4.0.2")
            version("koinComposeVersion", "4.0.2")
            library("koin-core", "io.insert-koin", "koin-core").versionRef("koinVersion")
            library("koin-android", "io.insert-koin", "koin-android").versionRef("koinVersion")
            library("koin-androidx-compose", "io.insert-koin", "koin-androidx-compose").versionRef("koinVersion")
            library("koin-compose", "io.insert-koin", "koin-compose").versionRef("koinComposeVersion")
            library("koin-compose-viewmodel", "io.insert-koin", "koin-compose-viewmodel").versionRef("koinComposeVersion")

            version("media3Version", "1.5.1")
            library("media3-exoplayer", "androidx.media3", "media3-exoplayer").versionRef("media3Version")
            library("media3-session", "androidx.media3", "media3-session").versionRef("media3Version")
            library("media3-ui", "androidx.media3", "media3-ui").versionRef("media3Version")
            library("media3-common", "androidx.media3", "media3-common").versionRef("media3Version")
            bundle(
                "media3",
                listOf(
                    "media3-exoplayer",
                    "media3-session",
                    "media3-ui",
                    "media3-common"
                )
            )

            // Additional Android Dependencies
            library("androidx-work-runtime", "androidx.work", "work-runtime-ktx").version("2.10.0")
            library("snapper", "dev.chrisbanes.snapper", "snapper").version("0.3.0")
        }
    }
}

rootProject.name = "JustListen"
include(":androidApp")
include(":shared")