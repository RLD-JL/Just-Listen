buildscript {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlinVersion.get()}")
        classpath("com.android.tools.build:gradle:7.2.1")
        classpath("com.squareup.sqldelight:gradle-plugin:1.5.3")
        classpath ("com.google.dagger:hilt-android-gradle-plugin:2.42")
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


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>{
    kotlinOptions {
        freeCompilerArgs = listOf("-Xuse-k2")
    }
}
