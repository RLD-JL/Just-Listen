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
            version("kotlinVersion", "2.0.10")

            version("ktorVersion", "2.0.2")
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

            version("composeVersion", "1.7.0")
            version("lifecycleVersion", "2.8.0")
            version("navigationVersion", "2.8.0-alpha10")
            
            // Compose
            library("compose-ui-util", "androidx.compose.ui", "ui-util").versionRef("composeVersion")
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("composeVersion")
            library("compose-ui-preview", "androidx.compose.ui", "ui-tooling-preview").versionRef("composeVersion")
            library("compose-material", "androidx.compose.material", "material").versionRef("composeVersion")
            library("compose-material3", "androidx.compose.material3", "material3").version("1.3.0")
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
            library("kotlinx-serialization-json", "org.jetbrains.kotlinx", "kotlinx-serialization-json").version("1.7.0")
            
            // Koin DI
            version("koinVersion", "4.0.0")
            version("koinComposeVersion", "4.0.0")
            library("koin-core", "io.insert-koin", "koin-core").versionRef("koinVersion")
            library("koin-android", "io.insert-koin", "koin-android").versionRef("koinVersion")
            library("koin-androidx-compose", "io.insert-koin", "koin-androidx-compose").versionRef("koinVersion")
            library("koin-compose", "io.insert-koin", "koin-compose").versionRef("koinComposeVersion")
            library("koin-compose-viewmodel", "io.insert-koin", "koin-compose-viewmodel").versionRef("koinComposeVersion")

            version("exoPlayerVersion", "2.19.1")
            library("exoplayer-mediasession", "com.google.android.exoplayer", "extension-mediasession").versionRef("exoPlayerVersion")
            library("exoplayer-core", "com.google.android.exoplayer", "exoplayer-core").versionRef("exoPlayerVersion")
            library("exoplayer-ui", "com.google.android.exoplayer", "exoplayer-ui").versionRef("exoPlayerVersion")
            bundle(
                "exoplayer",
                listOf(
                    "exoplayer-mediasession",
                    "exoplayer-core",
                    "exoplayer-ui"
                )
            )

            // Additional Android Dependencies
            library("androidx-work-runtime", "androidx.work", "work-runtime-ktx").version("2.9.0")
            library("accompanist-swiperefresh", "com.google.accompanist", "accompanist-swiperefresh").version("0.24.1-alpha")
            library("snapper", "dev.chrisbanes.snapper", "snapper").version("0.2.2")
        }
    }
}

rootProject.name = "JustListen"
include(":androidApp")
include(":shared")