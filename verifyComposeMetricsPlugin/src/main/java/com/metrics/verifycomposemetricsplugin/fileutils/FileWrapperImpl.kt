package com.metrics.verifycomposemetricsplugin.fileutils

import java.io.File


/**
 * FileWrapper
 *
 * Internal wrapper interface to read from file.
 *
 */
internal interface FileWrapper {
    fun readLines(): List<String>
}

/**
 * FileWrapperImpl
 *
 * Internal wrapper class to read from file.
 *
 * @property filePath Path of the file that you want to read from.
 */
internal class FileWrapperImpl(private val filePath: String): FileWrapper {
    override fun readLines(): List<String> {
        return File(filePath).readLines()
    }
}
