package com.metrics.verifycomposemetricsplugin

import com.metrics.verifycomposemetricsplugin.fileutils.FileWrapper
import org.junit.Assert.assertEquals
import org.junit.Test

class InferredUnstableClassCheckerTest {


    @Test(expected = InferredUnstableClassException::class)
    fun `WHEN inferredUnstableClasses IS above threshold SHOULD throw InferredUnstableClassException`() {
        val mockFileWrapper = MockFileWrapper(listOf(""))
        InferredUnstableClassChecker().inferredUnstableClassCheck(
            inferredUnstableClassThreshold = 0,
            inferredUnstableClasses = 1,
            classesFile = mockFileWrapper,
            errorAsWarning = false,
        )
    }

    @Test
    fun `WHEN inferredUnstableClass IS above threshold AND errorAsWarning SHOULD printToLog`() {
        val mockFileWrapper = MockFileWrapper(listOf(""))
        val status = InferredUnstableClassChecker().inferredUnstableClassCheck(
            inferredUnstableClassThreshold = 0,
            inferredUnstableClasses = 1,
            classesFile = mockFileWrapper,
            errorAsWarning = true,
        )
        val expected = InferredUnstableClassException(
            threshold = 0,
            inferredUnstableClasses = 1,
            possiblePlaces = ""
        ).message

        assertEquals("WARNING: $expected", status)
    }

    @Test
    fun `WHEN inferredUnstableClass IS below threshold SHOULD status return null`() {
        // If the status returns null, nothing will be printed in the log and
        // the gradle task will just succeed.
        val mockFileWrapper = MockFileWrapper(listOf(""))
        val status = InferredUnstableClassChecker().inferredUnstableClassCheck(
            inferredUnstableClassThreshold = 2,
            inferredUnstableClasses = 1,
            classesFile = mockFileWrapper,
            errorAsWarning = true,
        )

        assertEquals(null, status)
    }

    @Test
    fun `WHEN inferredUnstableClass IS equal to threshold SHOULD status return null`() {
        // If the status returns null, nothing will be printed in the log and
        // the gradle task will just succeed.
        val mockFileWrapper = MockFileWrapper(listOf(""))
        val status = InferredUnstableClassChecker().inferredUnstableClassCheck(
            inferredUnstableClassThreshold = 1,
            inferredUnstableClasses = 1,
            classesFile = mockFileWrapper,
            errorAsWarning = true,
        )
        assertEquals(null, status)
    }
}

class MockFileWrapper(private val readLines: List<String>) : FileWrapper {
    override fun readLines(): List<String> {
        return readLines
    }
}
