// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath(libs.kotlin.gradle.plugin.stnd)
        classpath("com.android.tools.build:gradle:8.1.4")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()

    }
}
