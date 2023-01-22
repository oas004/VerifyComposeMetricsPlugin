package com.metrics.verifycomposemetricsplugin

import com.metrics.verifycomposemetricsplugin.testutils.ComposeMetricsGradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class VerifyComposeMetricsTest {

    @Test
    fun `GIVEN implementation AND no Composables SHOULD yield SUCCESS outcome`() {
        val config = VerifyComposeMetricsConfigImpl(
            inferredUnstableClassThreshold = 0,
            errorAsWarning = false,
            shouldSkipMetricsGeneration = false,
            skipVerification = false,
            printMetricsInfo = false
        )
        val composableContent = ComposableContent(
            composableName = "TestScreen",
            unstableClassesAmount = 0,
            stableClassesAmount = 0,
        )
        val applicationModuleData = ApplicationModuleData(
            composeEnabled = true,
            verifyComposeMetricsConfig = config,
            composableContent = listOf(composableContent)
        )
        val projectCreator = TestProjectCreatorImpl().createTempApplicationProject(
            applicationModuleData = applicationModuleData,
            libraryModuleData = null,
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withArguments("verifyComposeMetrics")
            .withPluginClasspath()
            .forwardOutput()
            .build()
        assert(result.task(":app:verifyComposeMetrics")?.outcome == TaskOutcome.SUCCESS)
    }

    @Test
    fun `GIVEN unstable type UNDER threshold SHOULD succeed`() {
        val config = VerifyComposeMetricsConfigImpl(
            inferredUnstableClassThreshold = 3,
            errorAsWarning = false,
            shouldSkipMetricsGeneration = false,
            skipVerification = false,
            printMetricsInfo = true
        )
        val composableContent = ComposableContent(
            composableName = "TestScreen",
            unstableClassesAmount = 2,
            stableClassesAmount = 0,
        )
        val applicationModuleData = ApplicationModuleData(
            composeEnabled = true,
            verifyComposeMetricsConfig = config,
            composableContent = listOf(composableContent)
        )
        val projectCreator = TestProjectCreatorImpl().createTempApplicationProject(
            applicationModuleData = applicationModuleData,
            libraryModuleData = null,
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withArguments("verifyComposeMetrics")
            .withPluginClasspath()
            .forwardOutput()
            .build()
        assert(result.task(":app:verifyComposeMetrics")?.outcome == TaskOutcome.SUCCESS)

        val expectedOutcome = """
            Generating Compose Metrics Files
            inferredUnstableClasses = 2
            skippableComposables = 2
            restartableComposables = 2
            readonlyComposables = 0
            totalComposables = 2
            restartGroups = 2
            totalGroups = 2
            staticArguments = 0
            certainArguments = 0
            knownStableArguments = 0
            knownUnstableArguments = 0
            unknownStableArguments = 0
            totalArguments = 0
            markedStableClasses = 0
            inferredStableClasses = 1
            inferredUnstableClasses = 2
            inferredUncertainClasses = 0
            effectivelyStableClasses = 1
            totalClasses = 3
            memoizedLambdas = 1
            singletonLambdas = 0
            singletonComposableLambdas = 1
            composableLambdas = 1
            totalLambdas = 1
        """.trimIndent()

        assert(result.output.contains(expectedOutcome))

    }

    @Test
    fun `GIVEN unstableClasses ABOVE threshold SHOULD fail`() {
        val config = VerifyComposeMetricsConfigImpl(
            inferredUnstableClassThreshold = 3,
            errorAsWarning = false,
            shouldSkipMetricsGeneration = false,
            skipVerification = false,
            printMetricsInfo = false
        )
        val composableContent = ComposableContent(
            composableName = "TestScreen",
            unstableClassesAmount = 4,
            stableClassesAmount = 0,
        )
        val applicationModuleData = ApplicationModuleData(
            composeEnabled = true,
            verifyComposeMetricsConfig = config,
            composableContent = listOf(composableContent)
        )
        val projectCreator = TestProjectCreatorImpl().createTempApplicationProject(
            applicationModuleData = applicationModuleData,
            libraryModuleData = null,
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withArguments("verifyReleaseComposeMetrics")
            .withPluginClasspath()
            .buildAndFail()
            .task(":app:verifyReleaseComposeMetrics")?.outcome

        assertEquals(TaskOutcome.FAILED, result)
    }

    @Test
    fun `GIVEN unstableClasses ABOVE threshold AND skipverification true SHOULD succeed`() {
        val config = VerifyComposeMetricsConfigImpl(
            inferredUnstableClassThreshold = 3,
            errorAsWarning = false,
            shouldSkipMetricsGeneration = false,
            skipVerification = true,
            printMetricsInfo = false
        )
        val composableContent = ComposableContent(
            composableName = "TestScreen",
            unstableClassesAmount = 4,
            stableClassesAmount = 0,
        )
        val applicationModuleData = ApplicationModuleData(
            composeEnabled = true,
            verifyComposeMetricsConfig = config,
            composableContent = listOf(composableContent)
        )
        val projectCreator = TestProjectCreatorImpl().createTempApplicationProject(
            applicationModuleData = applicationModuleData,
            libraryModuleData = null,
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withArguments("verifyReleaseComposeMetrics")
            .withPluginClasspath()
            .build()
            .task(":app:verifyReleaseComposeMetrics")?.outcome

        assertEquals(TaskOutcome.SUCCESS, result)
    }

    @Test
    fun `GIVEN unstableClasses ABOVE threshold AND errorAsWarning true SHOULD succeed`() {
        val config = VerifyComposeMetricsConfigImpl(
            inferredUnstableClassThreshold = 3,
            errorAsWarning = true,
            shouldSkipMetricsGeneration = false,
            skipVerification = false,
            printMetricsInfo = false
        )
        val composableContent = ComposableContent(
            composableName = "TestScreen",
            unstableClassesAmount = 4,
            stableClassesAmount = 0,
        )
        val applicationModuleData = ApplicationModuleData(
            composeEnabled = true,
            verifyComposeMetricsConfig = config,
            composableContent = listOf(composableContent)
        )
        val projectCreator = TestProjectCreatorImpl().createTempApplicationProject(
            applicationModuleData = applicationModuleData,
            libraryModuleData = null,
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withArguments("verifyReleaseComposeMetrics")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":app:verifyReleaseComposeMetrics")?.outcome)

        val expectedWarning = """
            WARNING: You have added to many classes that is inferred unstable by the compose compiler. 
            You can either make the classes stable or increase your threshold in your gradle file 
            Using inferredUnstableClasses in Compose code can lead to performance issues. The current threshold is : 3 
            There is currently 4 inferredUnstableClasses in this module 
            The possible classes that has inferred unstable types are
             class UnstableClass_1 ,  class UnstableClass_2 ,  class UnstableClass_3 ,  class UnstableClass_4 
        """.trimIndent()

        // Check that it contains a warning
        assert(result.output.contains(expectedWarning))
    }

    @Test
    fun `GIVEN libraryModule implementation THEN gradle task SHOULD run`() {
        val config = VerifyComposeMetricsConfigImpl(
            inferredUnstableClassThreshold = 3,
            errorAsWarning = true,
            shouldSkipMetricsGeneration = false,
            skipVerification = false,
            printMetricsInfo = false
        )
        val applicationModuleData = ApplicationModuleData(
            composeEnabled = false,
            verifyComposeMetricsConfig = null,
            composableContent = listOf()
        )
        val composableContent = ComposableContent(
            composableName = "TestScreen",
            unstableClassesAmount = 4,
            stableClassesAmount = 0,
        )
        val libraryModuleData = LibraryModuleData(
            name = "lib",
            composeEnabled = true,
            verifyComposeMetricsConfig = config,
            composableContent = listOf(composableContent),
        )

        val projectCreator = TestProjectCreatorImpl().createTempApplicationProject(
            applicationModuleData = applicationModuleData,
            libraryModuleData = libraryModuleData,
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withArguments(":lib:verifyComposeMetrics")
            .withPluginClasspath()
            .forwardOutput()
            .build()

        assertEquals(TaskOutcome.SUCCESS, result.task(":lib:verifyComposeMetrics")?.outcome)

        val expectedWarning = """
            WARNING: You have added to many classes that is inferred unstable by the compose compiler. 
            You can either make the classes stable or increase your threshold in your gradle file 
            Using inferredUnstableClasses in Compose code can lead to performance issues. The current threshold is : 3 
            There is currently 4 inferredUnstableClasses in this module 
            The possible classes that has inferred unstable types are
             class UnstableClass_1 ,  class UnstableClass_2 ,  class UnstableClass_3 ,  class UnstableClass_4 
        """.trimIndent()

        // Check that it contains a warning
        assert(result.output.contains(expectedWarning))
    }

}
