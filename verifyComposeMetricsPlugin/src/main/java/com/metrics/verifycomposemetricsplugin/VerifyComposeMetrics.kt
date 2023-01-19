package com.metrics.verifycomposemetricsplugin

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.internal.tasks.factory.dependsOn
import com.metrics.verifycomposemetricsplugin.fileutils.FileWrapper
import com.metrics.verifycomposemetricsplugin.fileutils.FileWrapperImpl
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

public class VerifyComposeMetrics : Plugin<Project> {

    override fun apply(target: Project) {
        // Getting the extensions from the gradle file
        val extension = target.extensions.create(
            "verifyComposeMetricsConfig",
            VerifyComposeMetricsConfig::class.java
        )

        // Getting flag from task if we should generate the compose metrics files.
        // The compiler extension is configured to generate these files if this flag is passed.
        val shouldSkipMetricsGeneration = target.properties.containsKey("skipMetricsGeneration")

        val globalTask = target.tasks.register("verifyComposeMetrics")
        // Depending on this task. We need to do this to generate the metrics report
        target.tasks.withType(KotlinJvmCompile::class.java).whenTaskAdded {
            // By default we are generating the metrics report, but passing in
            // the flag above can bypass this.
            if (!shouldSkipMetricsGeneration) {
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
        // TODO: Check if this applies to LibraryAndroidComponentsExtension as well.
        target.extensions.getByType<ApplicationAndroidComponentsExtension>().beforeVariants {
            if (!it.enable) {
                return@beforeVariants
            }

            // Registering our verify task with the configuration passed from the user.
            // We are then adding a dependency for this to the compile kotlin release task.
            val variantTask = target.tasks.register<GenerateComposeMetricsTask>(
                name = "verify${it.name.capitalized()}ComposeMetrics",
            ) {
                this.errorAsWarning.set(extension.errorAsWarning)
                this.inferredUnstableClassThreshold.set(extension.inferredUnstableClassThreshold)
                this.printMetricsInfo.set(extension.printMetricsInfo)
                this.skipVerification.set(extension.skipVerification)
                this.variant.set(it.name)
                this.shouldSkipMetricsGeneration.set(shouldSkipMetricsGeneration)
            }
            variantTask.configure {
                this.dependsOn(target.tasks.named("compile${it.name.capitalized()}Kotlin"))
            }

            globalTask.dependsOn(variantTask)
        }
    }
}

public abstract class GenerateComposeMetricsTask : DefaultTask() {

    @get:Input
    public abstract val skipVerification: Property<Boolean>

    @get:Input
    public abstract val inferredUnstableClassThreshold: Property<Int>

    @get:Input
    public abstract val errorAsWarning: Property<Boolean>

    @get:Input
    public abstract val printMetricsInfo: Property<Boolean>

    @get:Input
    public abstract val variant: Property<String>

    @get:Input
    public abstract val shouldSkipMetricsGeneration: Property<Boolean>

    @TaskAction
    internal fun execute() {

        val moduleName = project.name
        val composeMetricsFolder =
            project.buildDir.listFiles()?.find { it.name == "compose_metrics" }
                ?: throw MissingComposeMetricsException()

        // TODO: Check if this file path will be correct for debug builds and make test
        val releaseModuleJson = composeMetricsFolder.listFiles()
            ?.find { it.name == "${moduleName}_${variant.get()}-module.json" }

        // TODO: Check if this file path will be correct for debug builds and make test
        val releaseClassesTxt = composeMetricsFolder.listFiles()
            ?.find { it.name == "${moduleName}_${variant.get()}-classes.txt" }

        if (releaseModuleJson == null || releaseClassesTxt == null) {
            throw MissingComposeMetricsException()
        }

        val metrics = Json.decodeFromString<Metrics>(releaseModuleJson.readText())

        if (!shouldSkipMetricsGeneration.get() && printMetricsInfo.get()) {
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

        val releaseClassesFile = FileWrapperImpl(releaseClassesTxt.path)

        if (!skipVerification.get()) {
            val status = InferredUnstableClassChecker().inferredUnstableClassCheck(
                inferredUnstableClassThreshold = inferredUnstableClassThreshold.get(),
                inferredUnstableClasses = metrics.inferredUnstableClasses,
                classesFile = releaseClassesFile,
                errorAsWarning = errorAsWarning.get(),
            )
            if (status != null) println(status)
        }
    }
}

internal class InferredUnstableClassChecker {
    /**
     * Checks if the generated inferred unstable classes is above the threshold. If it is, it
     * @throws IllegalStateException and provides possible classes that has this property.
     *
     * @param inferredUnstableClassThreshold
     * @param inferredUnstableClasses
     *
     * @throws InferredUnstableClassException
     * @throws NoSuchFileException
     *
     * returns status from the check
     */
    internal fun inferredUnstableClassCheck(
        inferredUnstableClassThreshold: Int,
        inferredUnstableClasses: Int,
        classesFile: FileWrapper,
        errorAsWarning: Boolean,
    ): String? {
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
                return "WARNING: ${exception.message}"
            } else {
                throw exception
            }
        }
        return null
    }
}

public interface VerifyComposeMetricsConfig {
    public val inferredUnstableClassThreshold: Property<Int>
    public val errorAsWarning: Property<Boolean>
    // TODO: Check if we should remove this flag as this is picked up by the property
    public val shouldSkipMetricsGeneration: Property<Boolean>
    public val skipVerification: Property<Boolean>
    public val printMetricsInfo: Property<Boolean>
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