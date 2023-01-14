package com.metrics.verifycomposemetricsplugin

import org.gradle.testkit.runner.GradleRunner
import spock.lang.Specification
import spock.lang.TempDir

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PluginTest extends Specification {

    @TempDir
    File testProjectDir
    File buildFile

    def Setup() {
        buildFile = new File(testProjectDir, "build.gradle.kts")
        buildFile << """
                plugins {
                    id("io.github.oas004.metrics") version "0.1.0-SNAPSHOT"
                }
           """
    }

    def "test test"() {
        buildFile << """
                verifyComposeMetricsConfig {
                    inferredUnstableClassThreshold.set(0)
                    errorAsWarning.set(false)
                    shouldSkipMetricsGeneration.set(false)
                    skipVerification.set(true)
                    printMetricsInfo.set(true)
                }
            """
        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir)
                .withPluginClasspath()
                .build()
        then:
        result.task(":verifyComposeMetrics").outcome == SUCCESS
    }
}