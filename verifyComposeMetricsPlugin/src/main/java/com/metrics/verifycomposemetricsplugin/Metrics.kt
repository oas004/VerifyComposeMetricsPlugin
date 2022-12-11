package com.metrics.verifycomposemetricsplugin

import kotlinx.serialization.Serializable

@Serializable
public data class Metrics(
    val skippableComposables: Int,
    val restartableComposables: Int,
    val readonlyComposables: Int,
    val totalComposables: Int,
    val restartGroups: Int,
    val totalGroups: Int,
    val staticArguments: Int,
    val certainArguments: Int,
    val knownStableArguments: Int,
    val knownUnstableArguments: Int,
    val unknownStableArguments: Int,
    val totalArguments: Int,
    val markedStableClasses: Int,
    val inferredStableClasses: Int,
    val inferredUnstableClasses: Int,
    val inferredUncertainClasses: Int,
    val effectivelyStableClasses: Int,
    val totalClasses: Int,
    val memoizedLambdas: Int,
    val singletonLambdas: Int,
    val singletonComposableLambdas: Int,
    val composableLambdas: Int,
    val totalLambdas: Int,
)
