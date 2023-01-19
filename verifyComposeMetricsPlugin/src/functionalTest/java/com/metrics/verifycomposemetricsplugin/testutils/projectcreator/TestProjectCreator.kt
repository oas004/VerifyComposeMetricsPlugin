package com.metrics.verifycomposemetricsplugin

import com.metrics.verifycomposemetricsplugin.testutils.projectcreator.ApplicationModuleWriter
import com.metrics.verifycomposemetricsplugin.testutils.projectcreator.ApplicationModuleWriterImpl
import java.io.File
import java.nio.file.Files
import kotlin.reflect.KClass
import kotlin.reflect.jvm.internal.impl.load.java.structure.JavaClass
import kotlin.reflect.jvm.internal.impl.load.kotlin.KotlinClassFinder.Result.KotlinClass

internal class VerifyComposeMetricsConfigImpl(
    val inferredUnstableClassThreshold: Int,
    val errorAsWarning: Boolean,
    val shouldSkipMetricsGeneration: Boolean,
    val skipVerification: Boolean,
    val printMetricsInfo: Boolean,
)

// If the composable content is null, there will not be rendered any
// compose code in the module.
internal data class ApplicationModuleData(
    val composeEnabled: Boolean,
    val verifyComposeMetricsConfig: VerifyComposeMetricsConfigImpl,
    val composableContent: List<ComposableContent>?,
)

internal data class ComposableContent(
    val composableName: String,
    val unstableClassesAmount: Int,
    val stableClassesAmount: Int,
)

internal data class LibraryModuleData(
    val composeEnabled: Boolean,
    val verifyComposeMetricsConfig: VerifyComposeMetricsConfigImpl,
)

internal interface TestProjectCreator {
    /**
     * Creates a project with an application module in a Temporary folder
     *
     * @param applicationModuleData Data that will be written in the application gradle file
     * @param libraryModuleData Data that will be written in the library gradle file.
     * If you do not provide a [LibraryModuleData], the library module will not be written.
     * @return Returns the files in the temp folder that is created.
     */
    fun createTempApplicationProject(
        applicationModuleData: ApplicationModuleData,
        libraryModuleData: LibraryModuleData?
    ): FileStructure
}

internal class TestProjectCreatorImpl : TestProjectCreator {

    // This class has the responsibility to make the project structure. The writing of the
    // content of the files are delegated to each writer.
    override fun createTempApplicationProject(
        applicationModuleData: ApplicationModuleData,
        libraryModuleData: LibraryModuleData?
    ): FileStructure {
        val tempDirPath = Files.createTempDirectory("verifyComposeMetrics")
        val tempDir = tempDirPath.toFile()

        val buildDir = File(tempDir, "build")
        val appDir = File(tempDir, "app")
        val appBuildDir = File(appDir, "build")
        val srcDir = File(appDir, "src")
        val mainDir = File(srcDir, "main")
        val javaDir = File(mainDir, "java")
        val comDir = File(javaDir, "com")
        val metricsDir = File(comDir, "metrics")
        val verifycomposemetricsDir = File(metricsDir, "verifycomposemetrics")

        if (
            !buildDir.mkdir() ||
            !appDir.mkdir() ||
            !srcDir.mkdir() ||
            !mainDir.mkdir() ||
            !javaDir.mkdir() ||
            !comDir.mkdir() ||
            !metricsDir.mkdir() ||
            !verifycomposemetricsDir.mkdir() ||
            !appBuildDir.mkdir()
        ) {
            throw IllegalStateException("IO Failure")
        }

        val buildFile = File(tempDir, "app/build.gradle.kts")
        buildFile.createNewFile()
        val settingsFile = File(tempDir, "settings.gradle")
        settingsFile.createNewFile()
        val projectFile = File(tempDir, "build.gradle.kts")
        projectFile.createNewFile()
        val manifestFile = File(mainDir, "AndroidManifest.xml")
        manifestFile.createNewFile()
        val gradleProperties = File(tempDir, "gradle.properties")
        gradleProperties.createNewFile()

        val mainActivity = File(verifycomposemetricsDir, "Main.kt")
        mainActivity.createNewFile()

        ApplicationModuleWriterImpl().apply {
            writeApplicationModule(
                gradleFile = buildFile,
                manifestFile = manifestFile,
                mainActivity = mainActivity,
                applicationModuleData = applicationModuleData
            )
        }

        gradleProperties.writeGradlePropertiesFile()

        projectFile.writeToProjectGradleFile()

        settingsFile.writeToSettingsGradleFile()


        return FileStructure(
            projectGradleFile = projectFile,
            srcGradleFile = buildFile,
            tempDir = tempDir,
            settingsGradleFile = settingsFile
        )
    }

    private fun File.writeToProjectGradleFile() {
        this.writeText(
            """
                    allprojects {
                        repositories {
                            google()
                            mavenCentral()
                        }
                    }
                """.trimIndent()
        )
    }

    private fun File.writeToSettingsGradleFile() {
        this.writeText(
            """
                    include ':app'
                    rootProject.name = "verifyComposeMetrics"
                """.trimIndent()
        )
    }

    private fun File.writeGradlePropertiesFile() {
        this.writeText(
            """
                android.useAndroidX=true
            """.trimIndent()
        )
    }
}


data class FileStructure(
    val projectGradleFile: File,
    val srcGradleFile: File,
    val tempDir: File,
    val settingsGradleFile: File,
)
