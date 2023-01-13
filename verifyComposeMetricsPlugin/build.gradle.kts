import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("plugin.serialization") version "1.7.20"
    `java-library`
    id("com.gradle.plugin-publish") version "1.0.0"
}

repositories {
    mavenCentral()
    google()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

dependencies {
    implementation(kotlin("stdlib", "1.7.20"))
    compileOnly("com.android.tools.build:gradle:7.3.1+")
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
}

project.tasks.withType<KotlinCompile>().configureEach {
    // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
    this.kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
    }
}

pluginBundle {
    website = "https://github.com/oas004/VerifyComposeMetricsPlugin"
    vcsUrl = "https://github.com/oas004/VerifyComposeMetricsPlugin.git"
    tags = listOf("android", "compose", "metrics")

    gradlePlugin {
        plugins {
            create("verify-compose-metrics") {
                version = "0.1.0"
                id = "io.github.oas004.metrics"
                displayName = "Verify Compose Metrics"
                description = "Small plugin to verify thresholds from the Compose metrics report."
                implementationClass = "com.metrics.verifycomposemetricsplugin.VerifyComposeMetrics"
            }
        }
    }
}
