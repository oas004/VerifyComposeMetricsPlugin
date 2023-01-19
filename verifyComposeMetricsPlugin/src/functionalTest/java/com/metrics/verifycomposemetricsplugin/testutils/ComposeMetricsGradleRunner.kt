package com.metrics.verifycomposemetricsplugin.testutils

import org.gradle.testkit.runner.GradleRunner

class ComposeMetricsGradleRunner {
    val runner: GradleRunner
        get() = GradleRunner.create().withGradleVersion("7.4.2").withDebug(true)
}
