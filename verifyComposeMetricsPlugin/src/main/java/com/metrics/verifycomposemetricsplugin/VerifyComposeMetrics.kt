package com.metrics.verifycomposemetricsplugin

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.com.esotericsoftware.minlog.Log
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Inject

public class VerifyComposeMetrics : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target
            .extensions
            .create<VerifyComposeMetricsConfigImpl>("VerifyComposeMetricsConfig")

        target.afterEvaluate {

            // Getting thresholds configured by the user
            val inferredUnstableClassThreshold = try {
                extension.inferredUnstableClassThreshold
                    ?: error(
                        "InferredUnstableClassThreshold is not set. " +
                                "Please add this in your gradle file"
                    )
            } catch (e: Exception) {
                error(
                    "We are not able to get the VerifyComposeMetricsConfig. Have you added" +
                            " this to your gradle file?"
                )
            }
            val errorAsWarning = extension.errorAsWarning

            // Getting flag from task if we should generate the compose metrics files.
            // The compiler extension is configured to generate these files if this flag is passed.
            val isGeneratingComposeMetrics =
                project.properties.containsKey("generateComposeMetrics")

            // Depending on this compile release task. We need to do this to generate the metrics report
            val assembleReleaseTask = tasks.named<KotlinJvmCompile>("compileReleaseKotlin")
            assembleReleaseTask.configure {
                if (isGeneratingComposeMetrics) {
                    kotlinOptions {
                        freeCompilerArgs = freeCompilerArgs + listOf(
                            "-P",
                            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                                    project.buildDir + "/compose_metrics"
                        )
                        freeCompilerArgs = freeCompilerArgs + listOf(
                            "-P",
                            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                                    project.buildDir + "/compose_metrics"
                        )
                    }
                }
            }

            // Registering our verify task with the configuration passed from the user.
            // We are then adding a dependency for this to the compile kotlin release task.
            val verifyTask by target.tasks.register<GenerateComposeMetricsTask>(
                name = "verifyComposeMetrics",
                isGeneratingComposeMetrics,
                inferredUnstableClassThreshold,
                errorAsWarning
            )
            verifyTask.dependsOn(assembleReleaseTask)

        }
    }
}

public abstract class GenerateComposeMetricsTask @Inject constructor(
    private val isGeneratingComposeMetrics: Boolean,
    private val inferredUnstableClassThreshold: Int,
    private val errorAsWarning: Boolean,
) : DefaultTask() {

    @TaskAction
    internal fun execute() {

        if (isGeneratingComposeMetrics) {
            println("Generating Compose Metrics Files")
        }

        val moduleName = project.name
        val composeMetricsFolder = project
            .buildDir
            .listFiles()
            ?.find { it.name == "compose_metrics" } ?: throw MissingComposeMetricsException()
        val releaseModuleJson =
            composeMetricsFolder.listFiles()
                ?.find { it.name == "${moduleName}_release-module.json" }

        val releaseClassesTxt =
            composeMetricsFolder.listFiles()
                ?.find { it.name == "${moduleName}_release-classes.txt" }

        if (releaseModuleJson == null || releaseClassesTxt == null) {
            throw MissingComposeMetricsException()
        }

        val metrics = Json.decodeFromString<Metrics>(releaseModuleJson.readText())

        inferredUnstableClassCheck(
            inferredUnstableClassThreshold = inferredUnstableClassThreshold,
            inferredUnstableClasses = metrics.inferredUnstableClasses,
            classesFile = releaseClassesTxt,
            errorAsWarning = errorAsWarning,
        )

    }

    /**
     * Checks if the generated inferred unstable classes is above the threshold. If it is, it
     * @throws IllegalStateException and provides possible classes that has this property.
     *
     * @param inferredUnstableClassThreshold
     * @param inferredUnstableClasses
     *
     */
    private fun inferredUnstableClassCheck(
        inferredUnstableClassThreshold: Int,
        inferredUnstableClasses: Int,
        classesFile: File,
        errorAsWarning: Boolean,
    ) {
        if (inferredUnstableClassThreshold < inferredUnstableClasses) {
            println(pluginTitle())

            val possiblePlaces = classesFile
                .readLines()
                .filter { it.contains("unstable") }
                .map { it.replace("{", "") }
                .map { it.replace("unstable", "") }

            val exception = InferredUnstableClassException(
                threshold = inferredUnstableClassThreshold,
                inferredUnstableClasses = inferredUnstableClasses,
                possiblePlaces = possiblePlaces.joinToString(),
            )
            if (errorAsWarning) {
                println("WARNING: ${exception.message}")
            } else {
                throw exception
            }
        }
    }
}

public open class VerifyComposeMetricsConfigImpl {
    public var inferredUnstableClassThreshold: Int? = null
    public var errorAsWarning: Boolean = false
}


private fun pluginTitle(): String {
    return """
        

        __      __       _  __          _____                                       __  __      _        _          
        \ \    / /      (_)/ _|        / ____|                                     |  \/  |    | |      (_)         
         \ \  / /__ _ __ _| |_ _   _  | |     ___  _ __ ___  _ __   ___  ___  ___  | \  / | ___| |_ _ __ _  ___ ___ 
          \ \/ / _ \ '__| |  _| | | | | |    / _ \| '_ ` _ \| '_ \ / _ \/ __|/ _ \ | |\/| |/ _ \ __| '__| |/ __/ __|
           \  /  __/ |  | | | | |_| | | |___| (_) | | | | | | |_) | (_) \__ \  __/ | |  | |  __/ |_| |  | | (__\__ \
            \/ \___|_|  |_|_|  \__, |  \_____\___/|_| |_| |_| .__/ \___/|___/\___| |_|  |_|\___|\__|_|  |_|\___|___/
                                __/ |                       | |                                                     
                               |___/                        |_|                                                     



    """.trimIndent()
}