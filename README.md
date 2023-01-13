# VerifyComposeMetricsPlugin
This is a small plugin that generates the compose metrics and checks that you have not crossed the threshold that you have set.

The Compose Metrics report is generated from the [Compiler report](https://github.com/androidx/androidx/blob/androidx-main/compose/compiler/design/compiler-metrics.md)

## Implementation

Add plugin to plugins block 

```kt
plugins {
    id("io.github.oas004.metrics") version "latest-version"
}
```
Adding this to your gradle file will add the gradle task `verifyComposeMetrics` to your task list. Running this task
will run the plugin for all your different flavours. If you for instance only would like to run it for the debug build you can run `./gradlew verifyDebugComposeMetrics`

Use the VerifyComposeMetricsConfig to configure the plugin.

```kt
VerifyComposeMetricsConfig {
    inferredUnstableClassThreshold = 0
    errorAsWarning = false
    generateComposeMetricsReport = true
    skipVerification = true
    printMetricsInfo = true
}

```

## Verifying inferred unstable classes

Unstable classes can be inferred by the Compose compiler, if a class has an argument that is considered unstable. This can lead to the composable using
the class not being skippable. This can in turn lead to perfomance issues down the line.
An example of an unstable class can be something like this 

```kt
data class UnstableClass(
    var greeting: String = "Hey this makes me an unstable type!"
)
```

This is because the class parameter is a var.

If you want to read more about `@Stable` classes and performance i would recommend [this](https://medium.com/androiddevelopers/jetpack-compose-stability-explained-79c10db270c8) blogpost.

If you want to limit the amount of inferred unstable classes, you can configure the `VerifyComposeMetricsConfig` with a `inferredUnstableClassThreshold`.
This will make the `./gradlew verifyComposeMetrics` task fail if the module has crossed the threshold. If you just want it to print a warning, you can 
use the `errorAsWarning` flag and set it to `true`.

## Generate the report

If you just want to generate the compose metrics report, I can suggest setting the `generateComposeMetricsReport` flag to true. This will generate a report in your build folder. The task for checking the thresholds also requires you to have a metrics report generated in your build file.

## Skip Verification
Using the `skipVerification` flag will skip all the threshold checks.

## Print the report
Setting the `printMetricsInfo` to `true` will print a version of the ComposeMetrics report to the log.

## Contributing
Plese contribute if you want! Feel free to open an issue if there is anything you want to discuss or anything is off.

## License

```
MIT License

Copyright (c) 2022 Odin Asbjørnsen

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
