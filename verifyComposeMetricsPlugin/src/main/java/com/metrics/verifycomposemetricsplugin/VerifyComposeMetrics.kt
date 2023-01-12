package com.metrics.verifycomposemetricsplugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import java.io.File
import javax.inject.Inject

public class VerifyComposeMetrics : Plugin<Project> {

    override fun apply(target: Project) {
        val extension =
            target.extensions.create<VerifyComposeMetricsConfigImpl>("VerifyComposeMetricsConfig")

        val skipVerification = extension.skipVerification

        val errorAsWarning = extension.errorAsWarning

        val printMetricsInfo = extension.printMetricsInfo

        // Getting flag from task if we should generate the compose metrics files.
        // The compiler extension is configured to generate these files if this flag is passed.
        val isGeneratingComposeMetrics =
            target.properties.containsKey("generateComposeMetrics") || extension.generateComposeMetricsReport

        val globalTask = target.tasks.register("verifyComposeMetrics")
        // Depending on this compile release task. We need to do this to generate the metrics report
        target.tasks.withType(KotlinJvmCompile::class.java).whenTaskAdded {
            if (isGeneratingComposeMetrics) {
                this.kotlinOptions {
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" + project.buildDir + "/compose_metrics"
                    )
                    freeCompilerArgs = freeCompilerArgs + listOf(
                        "-P",
                        "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" + project.buildDir + "/compose_metrics"
                    )
                }
            }
        }
        target.extensions.getByType<ApplicationAndroidComponentsExtension>().beforeVariants {
            if (!it.enable) {
                return@beforeVariants
            }
            // Getting thresholds configured by the user
            // If they want to skip verification they should not need to configure a threshold.
            val inferredUnstableClassThreshold = if (!skipVerification) {
                try {
                    extension.inferredUnstableClassThreshold ?: error("Missing inferredUnstableThreshold")
                } catch (e: Exception) {
                    throw MissingInferredUnstableClassFieldException()
                }
            } else {
                0
            }

            // Registering our verify task with the configuration passed from the user.
            // We are then adding a dependency for this to the compile kotlin release task.
            val variantTask = target.tasks.register<GenerateComposeMetricsTask>(
                name = "verify${it.name.capitalized()}ComposeMetrics",
                isGeneratingComposeMetrics,
                inferredUnstableClassThreshold,
                errorAsWarning,
                skipVerification,
                printMetricsInfo
            )
            variantTask.configure {
                this.dependsOn(target.tasks.named("compile${it.name.capitalized()}Kotlin"))
            }

            globalTask.dependsOn(variantTask)
        }
    }
}

public abstract class GenerateComposeMetricsTask @Inject constructor(
    private val isGeneratingComposeMetrics: Boolean,
    private val inferredUnstableClassThreshold: Int,
    private val errorAsWarning: Boolean,
    private val skipVerification: Boolean,
    private val printMetricsInfo: Boolean
) : DefaultTask() {

    @TaskAction
    internal fun execute() {

        val moduleName = project.name
        val composeMetricsFolder =
            project.buildDir.listFiles()?.find { it.name == "compose_metrics" }
                ?: throw MissingComposeMetricsException()
        val releaseModuleJson = composeMetricsFolder.listFiles()
            ?.find { it.name == "${moduleName}_release-module.json" }

        val releaseClassesTxt = composeMetricsFolder.listFiles()
            ?.find { it.name == "${moduleName}_release-classes.txt" }

        if (releaseModuleJson == null || releaseClassesTxt == null) {
            throw MissingComposeMetricsException()
        }

        val metrics = Json.decodeFromString<Metrics>(releaseModuleJson.readText())

        if (isGeneratingComposeMetrics && printMetricsInfo) {
            println("Generating Compose Metrics Files")
            println(
                """
                    inferredUnstableClasses = ${metrics.inferredUnstableClasses}
                    skippableComposables = ${metrics.skippableComposables}
                    restartableComposables = ${metrics.restartableComposables}
                    readonlyComposables = ${metrics.readonlyComposables}
                    totalComposables = ${metrics.totalComposables}
                    restartGroups = ${metrics.restartGroups}
                    totalGroups = ${metrics.totalGroups}
                    staticArguments = ${metrics.staticArguments}
                    certainArguments = ${metrics.certainArguments}
                    knownStableArguments = ${metrics.knownStableArguments}
                    knownUnstableArguments = ${metrics.knownUnstableArguments}
                    unknownStableArguments = ${metrics.unknownStableArguments}
                    totalArguments = ${metrics.totalArguments}
                    markedStableClasses = ${metrics.markedStableClasses}
                    inferredStableClasses = ${metrics.inferredStableClasses}
                    inferredUnstableClasses = ${metrics.inferredUnstableClasses}
                    inferredUncertainClasses = ${metrics.inferredUncertainClasses}
                    effectivelyStableClasses = ${metrics.effectivelyStableClasses}
                    totalClasses = ${metrics.totalClasses}
                    memoizedLambdas = ${metrics.memoizedLambdas}
                    singletonLambdas = ${metrics.singletonLambdas}
                    singletonComposableLambdas = ${metrics.singletonComposableLambdas}
                    composableLambdas = ${metrics.composableLambdas}
                    totalLambdas = ${metrics.totalLambdas}
                """.trimIndent()
            )
        }

        if (!skipVerification) {
            inferredUnstableClassCheck(
                inferredUnstableClassThreshold = inferredUnstableClassThreshold,
                inferredUnstableClasses = metrics.inferredUnstableClasses,
                classesFile = releaseClassesTxt,
                errorAsWarning = errorAsWarning,
            )
        }

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

            val possiblePlaces = classesFile.readLines().filter { it.contains("unstable") }
                .map { it.replace("{", "") }.map { it.replace("unstable", "") }

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

// TODO: Check if I can use Property instead.
public open class VerifyComposeMetricsConfigImpl {
    public var inferredUnstableClassThreshold: Int? = null
    public var errorAsWarning: Boolean = false
    public var generateComposeMetricsReport: Boolean = false
    public var skipVerification: Boolean = false
    public var printMetricsInfo: Boolean = false
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