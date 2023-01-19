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

val integrationTest: SourceSet by sourceSets.creating
val functionalTest: SourceSet by sourceSets.creating

dependencies {
    implementation(kotlin("stdlib", "1.7.20"))
    implementation("com.android.tools.build:gradle:7.3.1+")
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")

    // Test dependencies
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.3")

    // Integration test dependency
    "integrationTestImplementation"(project)
    "integrationTestImplementation"("org.junit.jupiter:junit-jupiter:5.8.2")
    "integrationTestImplementation"(gradleTestKit())
    "integrationTestImplementation"(kotlin("script-runtime"))

    // Functional test dependency
    "functionalTestImplementation"(project)
    "functionalTestImplementation"("org.junit.jupiter:junit-jupiter:5.8.2")
    "functionalTestImplementation"("org.jetbrains.kotlin:kotlin-test")
    "functionalTestImplementation"(gradleTestKit())
    "functionalTestImplementation"(kotlin("script-runtime"))
    "functionalTestImplementation"("com.android.tools.build:gradle:7.3.1")
}

project.tasks.withType<KotlinCompile>().configureEach {
    // Workaround for https://youtrack.jetbrains.com/issue/KT-37652
    if (!this.name.endsWith("TestKotlin")) {
        this.kotlinOptions {
            freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=strict"
        }
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
