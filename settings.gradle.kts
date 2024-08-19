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

            version("composeVersion", "1.5.4")
            library("compose-ui-util", "androidx.compose.ui", "ui-util").versionRef("composeVersion")
            library("compose-ui", "androidx.compose.ui", "ui").versionRef("composeVersion")
            library("compose-ui-preview", "androidx.compose.ui", "ui-tooling-preview").versionRef("composeVersion")
            library("compose-material", "androidx.compose.material", "material").versionRef("composeVersion")
            library("compose-animation", "androidx.compose.animation", "animation").versionRef("composeVersion")
            bundle(
                "compose",
                listOf(
                    "compose-ui",
                    "compose-ui-util",
                    "compose-ui-preview",
                    "compose-material",
                    "compose-animation"
                )
            )

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
        }
    }
}

rootProject.name = "Just Listen"
include(":androidApp")
include(":shared")