package com.metrics.verifycomposemetrics

import androidx.compose.material.Text
import androidx.compose.runtime.Composable

@Composable
fun Screen() {
    val state = ScreenState.Content()
    SubScreen(state)
}

@Composable
fun SubScreen(state: ScreenState) {
    Text(text = state.toString())
    val unstableState = UnstableClass()
    UnstableScreen(unstableClass = unstableState)
}

@Composable
fun UnstableScreen(unstableClass: UnstableClass) {
    Text(text = unstableClass.greeting)
}

sealed class ScreenState {
    data class Content(val greeting:String = "Hey! This class Should be immutable"): ScreenState()
}

data class UnstableClass(
    var greeting: String = "Hey this makes me an unstable type!"
)

data class UnstableGreetingsClass(
    var greeting: String = "Hey this makes me an unstable type!"
)