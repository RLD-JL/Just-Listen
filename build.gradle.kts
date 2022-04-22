buildscript {
    val compose_version by extra("1.2.0-alpha08")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    val kotlin_version = "1.6.20"

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.android.tools.build:gradle:7.0.4")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.0")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.38.1")

    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}