import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("plugin.serialization") version "1.7.20"
    `maven-publish`
    `java-library`
    signing
}

group = "com.metrics.verify.compose.metrics"
version = "1.0"

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

gradlePlugin {
    plugins {
        plugins.create("verify-compose-metrics") {
            id = "com.metrics.verify.compose.metrics"
            implementationClass = "com.metrics.verifycomposemetricsplugin.VerifyComposeMetrics"
        }
    }
}
