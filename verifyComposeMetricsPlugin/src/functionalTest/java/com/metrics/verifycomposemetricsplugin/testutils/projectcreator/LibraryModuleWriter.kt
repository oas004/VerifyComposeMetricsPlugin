package com.metrics.verifycomposemetricsplugin.testutils.projectcreator

import java.io.File

internal interface LibraryModuleWriter {
    /**
     * Writes the content of files for an application. Gradle files, screen etc.
     * based on the library input.
     *
     * @param libraryModuleData
     */
    fun writeLibraryModule(
        gradleFile: File,
        screenFile: File,
        libraryModuleData: LibraryModuleData
    )
}

internal class LibraryModuleWriterImpl : LibraryModuleWriter {
    override fun writeLibraryModule(
        gradleFile: File,
        screenFile: File,
        libraryModuleData: LibraryModuleData
    ) {

        gradleFile.writeSrcLibraryModule(
            name = libraryModuleData.name,
            composeEnabled = libraryModuleData.composeEnabled,
            inferredUnstableClassThreshold = libraryModuleData.verifyComposeMetricsConfig?.inferredUnstableClassThreshold
                ?: 0,
            errorAsWarning = libraryModuleData.verifyComposeMetricsConfig?.errorAsWarning ?: false,
            shouldSkipMetricsGeneration = libraryModuleData.verifyComposeMetricsConfig?.shouldSkipMetricsGeneration
                ?: false,
            skipVerification = libraryModuleData.verifyComposeMetricsConfig?.skipVerification
                ?: false,
            printMetricsInfo = libraryModuleData.verifyComposeMetricsConfig?.printMetricsInfo
                ?: false,
            pluginEnabled = libraryModuleData.verifyComposeMetricsConfig != null
        )
        screenFile.writeScreenFile(
            composableContent = libraryModuleData.composableContent,
            packageName = libraryModuleData.name
        )
    }

    private fun File.writeSrcLibraryModule(
        name: String,
        composeEnabled: Boolean,
        inferredUnstableClassThreshold: Int,
        errorAsWarning: Boolean,
        shouldSkipMetricsGeneration: Boolean,
        skipVerification: Boolean,
        printMetricsInfo: Boolean,
        pluginEnabled: Boolean
    ) {
        this.writeText(
            """   
            plugins {
                 id("com.android.library")
                 id("org.jetbrains.kotlin.android")
                 ${pluginsBlock(pluginEnabled)} 
            }
            
            verifyComposeMetricsConfig {
                inferredUnstableClassThreshold.set($inferredUnstableClassThreshold)
                errorAsWarning.set($errorAsWarning)
                shouldSkipMetricsGeneration.set($shouldSkipMetricsGeneration)
                skipVerification.set($skipVerification)
                printMetricsInfo.set($printMetricsInfo)
            }
            
            android {
                compileSdk = 33
                namespace = "com.metrics.$name"
                defaultConfig {
                    minSdk = 24
                    targetSdk = 33
            
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                }
                
                 buildTypes {
                     release {
                     }
                     debug {
                     }
                 }
                
                 buildFeatures {
                     compose = $composeEnabled
                 }
                
                 composeOptions {
                     kotlinCompilerExtensionVersion = "1.3.2"
                 }
                
                 compileOptions {
                     sourceCompatibility = JavaVersion.VERSION_1_8
                     targetCompatibility = JavaVersion.VERSION_1_8
                 }
                 kotlinOptions {
                     jvmTarget = "1.8"
                 }
            }
            
            dependencies {
                val composeBom = platform("androidx.compose:compose-bom:2022.10.00")
                implementation(composeBom)
                androidTestImplementation(composeBom)
            
                // Android Studio Preview support
                implementation("androidx.compose.ui:ui-tooling-preview")
                debugImplementation("androidx.compose.ui:ui-tooling")
                implementation("androidx.compose.material:material")
                implementation("androidx.compose.foundation:foundation")
                implementation("androidx.compose.ui:ui")

                implementation("androidx.core:core-ktx:1.8.0")
                implementation("androidx.appcompat:appcompat:1.5.1")
                implementation("com.google.android.material:material:1.7.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
            }
        
            """.trimIndent()
        )
    }

    private fun File.writeScreenFile(
        composableContent: List<ComposableContent>?,
        packageName: String
    ) {
        if (composableContent != null) {


            this.writeText(
                """
            package com.metrics.$packageName
            
            import androidx.compose.material.Text
            import androidx.compose.runtime.Composable
            
            @Composable        
            fun Screen() {
                    ${
                    composableContent.map { functionInvoked(it.composableName) }.toString()
                        .removeSurrounding(prefix = "[", suffix = "]").replace(",", "\n")
                }
            }
            
                """.trimIndent()
            )
            composableContent.forEach {
                val stableClassNames = (1 until it.stableClassesAmount + 1)
                    .map { classInvoked("StableClass_$it") }

                val unstableClassNames = (1 until it.unstableClassesAmount + 1)
                    .map { classInvoked("UnstableClass_$it") }

                this.appendText(
                    """

            @Composable
            fun ${it.composableName}() {
                ${
                        unstableClassNames
                            .toString()
                            .removeSurrounding(prefix = "[", suffix = "]")
                            .replace(",", "\n")
                    }
                    
                ${
                        stableClassNames
                            .toString()
                            .removeSurrounding(prefix = "[", suffix = "]")
                            .replace(",", "\n")
                    }
            }
                   
                """.trimIndent().trimIndent()
                )
                (1 until it.unstableClassesAmount + 1).forEach {
                    this.appendText(
                        """
                        
                        data class UnstableClass_$it(var typeName: String = "Unstable")
                        
                    """.trimIndent()
                    )
                }
                (1 until it.stableClassesAmount + 1).forEach {
                    this.appendText(
                        """
                        
                        data class StableClass_$it(val typeName: String = "Stable")
                        
                    """.trimIndent()
                    )
                }
            }
        }
    }

    private fun functionInvoked(functionName: String): String {
        return "$functionName()"
    }

    private fun pluginsBlock(pluginEnabled: Boolean): String {
        return if (pluginEnabled) {
            "id(\"io.github.oas004.metrics\") version \"0.1.1-SNAPSHOT\""
        } else ""
    }

    private fun classInvoked(className: String): String {
        return "val ${className.toLowerCase()}: ${className.capitalize()} = ${className.capitalize()}()"
    }
}