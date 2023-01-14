package com.metrics.verifycomposemetricsplugin

import org.gradle.api.GradleException

/**
 * MissingComposeMetricsException
 *
 * Exception that should indicate to the user that they have not generated
 * any compose metric files.
 *
 */
public class MissingComposeMetricsException: GradleException() {
    override val message: String
        get() =  "You are trying to verify compose metrics but the metrics files does not exist " +
                "in your build folder. This might be a bug in the VerifyComposeMetricsPlugin."
}
