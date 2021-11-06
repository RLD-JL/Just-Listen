buildscript {
    val compose_version by extra("1.1.0-beta01")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.31")
        classpath("com.android.tools.build:gradle:7.0.3")
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