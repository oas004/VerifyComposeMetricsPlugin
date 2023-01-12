package com.metrics.verifycomposemetricsplugin

import org.gradle.api.GradleException

/**
 * ComposeMetricsConfigurationException
 *
 * This should be thrown if there is anything wrong with the Configuration of the plugin.
 *
 */
public abstract class ComposeMetricsConfigurationException(): GradleException()

/**
 * MissingInferredUnstableClassException
 *
 * If you have not configured the inferredUnstableClass field from Gradle and you are trying
 * to do a verification step, then this should be thrown.
 *
 */
public class MissingInferredUnstableClassFieldException() : ComposeMetricsConfigurationException() {
    override val message: String
        get() = "We are not able to get the inferredUnstableClassThreshold config. Have you added" +
                " this to your gradle file? If you just want to generate the metrics report you " +
                "can add the skipVerification flag instead."
}
