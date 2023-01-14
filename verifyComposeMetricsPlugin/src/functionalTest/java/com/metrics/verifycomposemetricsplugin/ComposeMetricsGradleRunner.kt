package com.metrics.verifycomposemetricsplugin

import org.gradle.testkit.runner.GradleRunner

class ComposeMetricsGradleRunner {
    val runner: GradleRunner
        get() = GradleRunner.create().withGradleVersion("7.3.1")
}
