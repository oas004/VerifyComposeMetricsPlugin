# IMPORTANT
This project is currently very experimental and is not yet meant to be used in production by anyone.

![Build and Test](https://github.com/oas004/VerifyComposeMetricsPlugin/actions/workflows/build.yml/badge.svg)

# VerifyComposeMetricsPlugin

This is a small plugin that generates the compose metrics and checks that you have not crossed the threshold that you have set.
The point of this plugin is to help apps that might suffer from performance issues due to inferred unstable objects being passed through 
Composable functions. It can also help to get an overview of all metrics related to Compose in your app. Furthermore, the point here is to set
a baseline for the current state of the project and run the gradle command on CI. This way you can gain control over all the code submitted that 
might cause performance issues down the line.

This library is meant to be used in UI modules.

If you have not seen any performance issues in your app, you might not have to think about this at all.

The Compose Metrics report is generated from the [Compiler report](https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md)

## Requirements

 - Compose Compiler version 1.2.0+
 - Compose enabled
 - AGP Version 7.3.1+

## Implementation

Add plugin to plugins block 

```kts
plugins {
    id("io.github.oas004.metrics") version "latest-version"
}
```

If you want to use the legacy plugin application, you can use
```kts
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("io.github.oas004.metrics:verifyComposeMetricsPlugin:latest-version")
  }
}

apply(plugin = "io.github.oas004.metrics")
```


Adding this to your gradle file will add the gradle task `verifyComposeMetrics` to your task list. Running this task
will run the plugin for all your different flavours. If you for instance only would like to run it for the debug build you can run `./gradlew verifyDebugComposeMetrics`

Use the VerifyComposeMetricsConfig to configure the plugin.

```kt
VerifyComposeMetricsConfig {
    inferredUnstableClassThreshold.set(0)
    errorAsWarning.set(false)
    shouldSkipMetricsGeneration.set(false)
    skipVerification.set(true)
    printMetricsInfo.set(true)
}

```

In order to use the plugin, you will have to enable compose in the project.

## Verifying inferred unstable classes

Unstable classes can be inferred by the Compose compiler, if a class has an argument that is considered unstable. This can lead to the composable using
the class not being skippable. This can in turn lead to performance issues down the line.
An example of an unstable class can be something like this 

```kt
data class UnstableClass(
    var greeting: String = "Hey this makes me an unstable type!"
)
```

This is because the class parameter is a var.

If you want to read more about `@Stable` classes and performance I would recommend [this](https://medium.com/androiddevelopers/jetpack-compose-stability-explained-79c10db270c8) blogpost.

If you want to limit the amount of inferred unstable classes, you can configure the `VerifyComposeMetricsConfig` with a `inferredUnstableClassThreshold`.
This will make the `./gradlew verifyComposeMetrics` task fail if the module has crossed the threshold. If you just want it to print a warning, you can 
use the `errorAsWarning` flag and set it to `true`.

### Example

If you have a screen with composable functions like this:

```kt
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

```
And then adding a verifyComposeMetrics configuration into build.gradle.kts like this:

```kts
verifyComposeMetricsConfig {
    inferredUnstableClassThreshold.set(0)
    errorAsWarning.set(false)
    shouldSkipMetricsGeneration.set(false)
    skipVerification.set(false)
    printMetricsInfo.set(true)
}
```

running `./gradlew verifyComposeMetrics` will fail for all build flavours with

```
Execution failed for task ':app:verifyReleaseComposeMetrics'.
> You have added to many classes that is inferred unstable by the compose compiler. 
  You can either make the classes stable or increase your threshold in your gradle file 
  Using inferredUnstableClasses in Compose code can lead to performance issues. The current threshold is : 0 
  There is currently 2 inferredUnstableClasses in this module 
  The possible classes that has inferred unstable types are
   class UnstableClass ,  class UnstableGreetingsClass 

```

## Generate the report

If you want to skip generating the report, you can pass in the flag `-PskipMetricsGeneration` otherwise, it will be generated by default.

## Skip Verification
Using the `skipVerification` flag will skip all the threshold checks.

## Print the report
Setting the `printMetricsInfo` to `true` will print a version of the ComposeMetrics report to the log.

## Contributing
Please contribute if you want! Feel free to open an issue if there is anything you want to discuss or anything is off.

## Future work
Checking inferred unstable classes might not be enough. We kinda want to know the functions that are restartable and not skippable in order to make them skippable.
I would also like to maybe use the metrics report to provide more guidance on how use this to improve performance.

## License

```
MIT License

Copyright (c) 2023 Odin Asbjørnsen

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
