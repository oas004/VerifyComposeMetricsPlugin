import org.gradle.kotlin.dsl.`kotlin-dsl`
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
    `kotlin-dsl`
    `java-gradle-plugin`
    kotlin("plugin.serialization") version "1.7.20"
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

//tasks.withType<KotlinCompile>().configureEach {
//    if (project.findProperty("generateComposeMetricsReport") == "true") {
//        kotlinOptions {
//            freeCompilerArgs += listOf(
//                "-P",
//                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
//                        project.projectDir.absolutePath + "/compose_metrics"
//            )
//            freeCompilerArgs += listOf(
//                "-P",
//                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
//                        project.projectDir.absolutePath + "/compose_metrics"
//            )
//        }
//    }
//}

dependencies {
    implementation(kotlin("stdlib", "1.7.20"))
    implementation(gradleApi())
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin-api:1.7.20")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
}
gradlePlugin {
    plugins {
        plugins.register("verify-compose-metrics") {
            id = "com.metrics.verify.compose.metrics"
            implementationClass = "com.metrics.verifycomposemetricsplugin.VerifyComposeMetrics"
        }
    }
}
