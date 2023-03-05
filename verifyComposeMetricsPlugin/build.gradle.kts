import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    `java-library`
    id("com.gradle.plugin-publish") version libs.versions.gradle.publish.plugin.get()
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

val integrationTest: SourceSet by sourceSets.creating
val functionalTest: SourceSet by sourceSets.creating

dependencies {
    implementation(kotlin("stdlib", libs.versions.kotlin.get()))
    implementation(libs.agp.tools)
    implementation(gradleApi())
    implementation(libs.kotlin.gradle.plugin.api)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlin.gradle.plugin.stnd)

    // Test dependencies
    testImplementation(libs.junit.stnd)
    testImplementation(libs.mockk)
    testImplementation(libs.junit.jupiter)

    // Integration test dependency
    "integrationTestImplementation"(project)
    "integrationTestImplementation"(libs.junit.jupiter)
    "integrationTestImplementation"(gradleTestKit())
    "integrationTestImplementation"(kotlin("script-runtime"))

    // Functional test dependency
    "functionalTestImplementation"(project)
    "functionalTestImplementation"(libs.junit.jupiter)
    "functionalTestImplementation"(libs.kotlin.test)
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlin("script-runtime"))
    "functionalTestImplementation"(libs.agp.tools)
}

project.tasks.withType<KotlinCompile>().configureEach {
    // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
    if (!this.name.endsWith("TestKotlin")) {
        this.kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
        }
    }
}

version = "0.1.2"
group = "io.github.oas004.metrics"

pluginBundle {
    website = "https://github.com/oas004/VerifyComposeMetricsPlugin"
    vcsUrl = "https://github.com/oas004/VerifyComposeMetricsPlugin.git"
    tags = listOf("android", "compose", "metrics")

    gradlePlugin {
        plugins {
            create("verify-compose-metrics") {
                id = "io.github.oas004.metrics"
                displayName = "Verify Compose Metrics"
                description = "Small plugin to verify thresholds from the Compose metrics report."
                implementationClass = "com.metrics.verifycomposemetricsplugin.VerifyComposeMetrics"
                testSourceSets(functionalTest)
            }
        }
    }
}

// Running integration tests
val integrationTestTask = tasks.register<Test>("integrationTest") {
    description = "Runs the integration tests."
    group = "verification"
    testClassesDirs = integrationTest.output.classesDirs
    classpath = integrationTest.runtimeClasspath
    mustRunAfter(tasks.test)
}

// Running integration tests
val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs the functional tests."
    group = "verification"
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    mustRunAfter(tasks.test)
}

tasks.check {
    dependsOn(integrationTestTask)
    dependsOn(functionalTestTask)
}

tasks.withType<Test>().configureEach {
    // Using JUnitPlatform for running tests
    useJUnitPlatform()
}
