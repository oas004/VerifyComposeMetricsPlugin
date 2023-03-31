plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("io.github.oas004.metrics") version "0.1.2-SNAPSHOT"
}

android {
    namespace = "com.metrics.lib"
    compileSdk = 33

    defaultConfig {
        minSdk = 24
        targetSdk = 33

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

verifyComposeMetricsConfig {
    inferredUnstableClassThreshold.set(0)
    errorAsWarning.set(false)
    shouldSkipMetricsGeneration.set(false)
    skipVerification.set(false)
    printMetricsInfo.set(true)
}

dependencies {
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.activity)

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
}