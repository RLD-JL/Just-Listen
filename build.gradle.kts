buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    val kotlin_version = "1.6.21"

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.40.1")

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

/*
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>{
    kotlinOptions {
        freeCompilerArgs = listOf("-Xuse-k2")
    }
}*/
