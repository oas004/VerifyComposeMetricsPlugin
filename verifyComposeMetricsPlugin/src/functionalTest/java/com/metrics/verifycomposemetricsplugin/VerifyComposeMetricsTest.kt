package com.metrics.verifycomposemetricsplugin

import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class VerifyComposeMetricsTest {

    @TempDir
    lateinit var testProjectDir: File

    @Test
    fun test() {
        val projectCreator = TestProjectCreatorImpl().createTempProject(testProjectDir)
        projectCreator.srcGradleFile.appendText(
            """
                verifyComposeMetricsConfig {
                    inferredUnstableClassThreshold.set(0)
                    errorAsWarning.set(true)
                    shouldSkipMetricsGeneration.set(false)
                    skipVerification.set(true)
                    printMetricsInfo.set(true)
                }
            """.trimIndent()
        )

        val result = ComposeMetricsGradleRunner()
            .runner
            .withProjectDir(projectCreator.tempDir)
            .withPluginClasspath()
            .build()
        assert(result.task(":app:verifyComposeMetrics")?.outcome == TaskOutcome.SUCCESS)
    }

}
