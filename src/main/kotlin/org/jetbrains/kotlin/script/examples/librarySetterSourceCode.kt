package org.jetbrains.kotlin.script.examples

import java.io.File
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.toScriptSource

// This is required to set the path of the .dylib files
fun libraryPathSetterSourceCode(libraryFolder: File): SourceCode {
    val setter = """
        System.setProperty("java.library.path", "${libraryFolder.absolutePath}:" + System.getProperty("java.library.path"))
        ClassLoader::class.java.getDeclaredField("sys_paths").run { 
            isAccessible = true
            set(null, null)
            isAccessible = false
        }
    """.trimIndent()

    return createTempFile(prefix = "CodeGen", suffix = ".$extension.kts", directory = libraryFolder)
        .apply { writeText(setter) }
        .apply { deleteOnExit() }
        .toScriptSource()
}