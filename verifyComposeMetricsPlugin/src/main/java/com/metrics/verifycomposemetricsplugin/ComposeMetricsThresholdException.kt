package com.metrics.verifycomposemetricsplugin

import org.gradle.api.GradleException

/**
 * ComposeMetricsThresholdException
 *
 * Should be thrown when the compose metrics verification exceeds the set threshold.
 *
 */
public abstract class ComposeMetricsThresholdException: GradleException()

/**
 * InferredUnstableClassException
 *
 * Should be thrown when the compose metrics verification for InferredUnstableClasses are above
 * the threshold.
 *
 * @property threshold
 * @property inferredUnstableClasses
 * @property possiblePlaces
 */
public class InferredUnstableClassException(
    private val threshold: Int,
    private val inferredUnstableClasses: Int,
    private val possiblePlaces: String,
): ComposeMetricsThresholdException() {
    override val message: String
        get() = "You have added to many classes that is inferred unstable by the compose compiler." +
                " \nYou can either make the classes stable or increase your threshold " +
                "in your gradle file \n" +
                "Using inferredUnstableClasses in Compose code can lead to performance issues. " +
                "The current threshold is : $threshold \n" +
                "There is currently $inferredUnstableClasses inferredUnstableClasses " +
                "in this module \n" +
                "The possible classes that has inferred unstable types are\n" +
                possiblePlaces
}
