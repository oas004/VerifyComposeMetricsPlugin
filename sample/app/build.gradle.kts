plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.oas004.metrics") version "0.1.2-SNAPSHOT"
}

android {
    compileSdk = 33
    namespace = "com.metrics.verifycomposemetrics"

    defaultConfig {
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
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