package org.jetbrains.kotlin.script.examples.interop

import java.io.File

data class PackageName(private val parts: List<String>) {
    val name: String
        get() = parts.joinToString(".")

    val folder: String
        get() = parts.joinToString(File.separator)
}