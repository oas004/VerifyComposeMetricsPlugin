package com.metrics.verifycomposemetricsplugin.testutils.projectcreator

import java.io.File

internal interface ApplicationModuleWriter {
    /**
     * Writes the content of files for an application. Gradle files, manifest, mainActivity etc.
     * based on the applicationModuleData input.
     *
     * @param applicationModuleData
     */
    fun writeApplicationModule(
        gradleFile: File,
        manifestFile: File,
        mainActivity: File,
        applicationModuleData: ApplicationModuleData
    )
}

internal class ApplicationModuleWriterImpl : ApplicationModuleWriter {
    override fun writeApplicationModule(
        gradleFile: File,
        manifestFile: File,
        mainActivity: File,
        applicationModuleData: ApplicationModuleData
    ) {
        gradleFile.writeSrcApplicationModule(
            composeEnabled = applicationModuleData.composeEnabled,
            inferredUnstableClassThreshold = applicationModuleData.verifyComposeMetricsConfig?.inferredUnstableClassThreshold
                ?: 0,
            errorAsWarning = applicationModuleData.verifyComposeMetricsConfig?.errorAsWarning
                ?: false,
            shouldSkipMetricsGeneration = applicationModuleData.verifyComposeMetricsConfig?.shouldSkipMetricsGeneration
                ?: false,
            skipVerification = applicationModuleData.verifyComposeMetricsConfig?.skipVerification
                ?: false,
            printMetricsInfo = applicationModuleData.verifyComposeMetricsConfig?.printMetricsInfo
                ?: false,
            pluginEnabled = applicationModuleData.verifyComposeMetricsConfig != null
        )
        manifestFile.writeToManifestFile()
        mainActivity.writeToMainActivityFile(applicationModuleData.composableContent)
    }

    private fun File.writeToMainActivityFile(composableContent: List<ComposableContent>?) {
        if (composableContent != null) {


            this.writeText(
                """
            package com.metrics.verifycomposemetrics
            
            import android.os.Bundle
            import android.os.PersistableBundle
            import androidx.activity.ComponentActivity
            import androidx.compose.material.Text
            import androidx.compose.runtime.Composable
            import androidx.activity.compose.setContent
           
            class Main: ComponentActivity() {
                override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
                    super.onCreate(savedInstanceState, persistentState)
                    setContent {
                    ${
                    composableContent.map { functionInvoked(it.composableName) }.toString()
                        .removeSurrounding(prefix = "[", suffix = "]").replace(",", "\n")
                }
                    }
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
        } else {
            this.writeText(
                """
                    package com.metrics.verifycomposemetrics
                    
                    import android.os.Bundle
                    import android.os.PersistableBundle
                    import androidx.activity.ComponentActivity
    
                    class Main: ComponentActivity() {
                        override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
                            super.onCreate(savedInstanceState, persistentState)
                        }
                    }
                """.trimIndent()
            )
        }
    }

    private fun File.writeToManifestFile() {
        this.writeText(
            """
                    <?xml version="1.0" encoding="utf-8"?>
                    <manifest xmlns:android="http://schemas.android.com/apk/res/android" xmlns:tools="http://schemas.android.com/tools" >
                            <application
                                android:allowBackup="true"
                                android:label="test"
                                android:supportsRtl="true"
                                tools:targetApi="33" >
                                <activity
                                    android:name=".Main"
                                    android:launchMode="singleInstance"
                                    android:exported="true">
                                    <intent-filter>
                                        <action android:name="android.intent.action.MAIN" />
                        
                                        <category android:name="android.intent.category.LAUNCHER" />
                                    </intent-filter>
                                </activity>
                            </application>
                    </manifest>
            
                """.trimIndent()
        )
    }

    private fun File.writeSrcApplicationModule(
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
                 id("com.android.application")
                 id("org.jetbrains.kotlin.android")
                 ${pluginsBlock(pluginEnabled)} 
            }
            
            ${
                pluginConfigurationsBlock(
                    pluginEnabled = pluginEnabled,
                    inferredUnstableClassThreshold = inferredUnstableClassThreshold,
                    errorAsWarning = errorAsWarning,
                    shouldSkipMetricsGeneration = shouldSkipMetricsGeneration,
                    skipVerification = skipVerification,
                    printMetricsInfo = printMetricsInfo
                )
            }
            
            android {
                compileSdk = 33
                namespace = "com.metrics.verifycomposemetrics"
                defaultConfig {
                    minSdk = 24
                    targetSdk = 33
                    versionCode = 1
                    versionName = "1.0"
            
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
                     kotlinCompilerExtensionVersion = "1.4.2"
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
                val composeBom = platform("androidx.compose:compose-bom:2023.03.00")
                implementation(composeBom)
                androidTestImplementation(composeBom)
            
                // Android Studio Preview support
                implementation("androidx.compose.ui:ui-tooling-preview")
                debugImplementation("androidx.compose.ui:ui-tooling")
                implementation("androidx.compose.material:material")
                implementation("androidx.compose.foundation:foundation")
                implementation("androidx.compose.ui:ui")
                implementation("androidx.activity:activity-compose:1.7.0-alpha03")

                implementation("androidx.core:core-ktx:1.8.0")
                implementation("androidx.appcompat:appcompat:1.5.1")
                implementation("com.google.android.material:material:1.7.0")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.7.20")
            }
        
            """.trimIndent()
        )
    }

    private fun functionInvoked(functionName: String): String {
        return "$functionName()"
    }

    private fun pluginsBlock(pluginEnabled: Boolean): String {
        return if (pluginEnabled) {
            "id(\"io.github.oas004.metrics\")"
        } else ""
    }

    private fun pluginConfigurationsBlock(
        pluginEnabled: Boolean,
        inferredUnstableClassThreshold: Int,
        errorAsWarning: Boolean,
        shouldSkipMetricsGeneration: Boolean,
        skipVerification: Boolean,
        printMetricsInfo: Boolean,
    ): String {
        return if (pluginEnabled) {
            """
                verifyComposeMetricsConfig {
                inferredUnstableClassThreshold.set($inferredUnstableClassThreshold)
                errorAsWarning.set($errorAsWarning)
                shouldSkipMetricsGeneration.set($shouldSkipMetricsGeneration)
                skipVerification.set($skipVerification)
                printMetricsInfo.set($printMetricsInfo)
            }
            """.trimIndent()
        } else {
            ""
        }
    }

    private fun classInvoked(className: String): String {
        return "val ${className.toLowerCase()}: ${className.capitalize()} = ${className.capitalize()}()"
    }

}