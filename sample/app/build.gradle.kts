plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.github.oas004.metrics") version "0.1.0-SNAPSHOT"
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
        kotlinCompilerExtensionVersion = "1.3.2"
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
    skipVerification.set(true)
    printMetricsInfo.set(true)
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2022.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Android Studio Preview support
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.compose.material:material")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.ui:ui")

    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.0")
}