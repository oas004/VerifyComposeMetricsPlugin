package com.metrics.verifycomposemetricsplugin

import java.io.File
import java.nio.file.Files

interface TestProjectCreator {
    fun createTempProject(tempProjectDir: File): FileStructure
}

class TestProjectCreatorImpl: TestProjectCreator {
    override fun createTempProject(tempProjectDir: File): FileStructure {
        val tempDirPath = Files.createTempDirectory("VerifyComposeMetrics")
        val tempDir = tempDirPath.toFile()

        val buildDir = File(tempDir, "build")

        if (!buildDir.mkdir()) {
            throw IllegalStateException("IO Failure")
        }

        val buildFile = File(tempDir, "build.gradle.kts")
        buildFile.createNewFile()
        val settingsFile = File(tempDir, "settings.gradle")
        settingsFile.createNewFile()
        val projectFile = File(tempDir, "build.gradle.kts")
        projectFile.createNewFile()

        projectFile.writeText(
            """
                 buildscript {
                    repositories {
                        google()
                        mavenCentral()
                        maven(url = "https://plugins.gradle.org/m2/")
                        maven(url = "https://jitpack.io")
                    }
                    dependencies {
                        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")
                        classpath("com.android.tools.build:gradle:7.3.1")
                    }
                }

                allprojects {
                    repositories {
                        google()
                        mavenCentral()
                    }
                }     
            """.trimIndent()
        )

        settingsFile.writeText(
            """
                include ':app'
                rootProject.name = "verifyComposeMetrics"
            """.trimIndent()
        )

        buildFile.writeText(
            """      
            plugins {
                id("com.android.library")
                id("org.jetbrains.kotlin.android")
                id("io.github.oas004.metrics") version "0.1.0-SNAPSHOT"
            }

            android {
                buildTypes {
                   release {
                   }
                }
            }
        """.trimIndent()
        )
        return FileStructure(
            projectGradleFile = projectFile,
            srcGradleFile = buildFile,
            tempDir = tempDir
            //settingsGradleFile = settingsFile
        )
    }
}

data class FileStructure(
    val projectGradleFile: File,
    val srcGradleFile: File,
    val tempDir: File,
    //val settingsGradleFile: File,
)
