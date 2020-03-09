package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.toScriptSource
import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed class InteropLib {
    class Definition(val file: File) : InteropLib()
    class HeaderFile(val file: File) : InteropLib()
}

@ExperimentalCoroutinesApi
suspend fun InteropLib.Definition.sourceCode(): SourceCode {
    val parentFolder = createTempDir("CInterOp", suffix = "")
        .also { it.mkdirs() }
        .also { it.deleteOnExit() }

    val copied = File(parentFolder, file.name).also { it.deleteOnExit() }
    file.copyTo(copied)

    shell(dir = parentFolder) {
        "cinterop -def ${copied.name}"()
    }

    return copied.toScriptSource()
}

fun InteropLib.definition(): InteropLib.Definition = when (this) {
    is InteropLib.Definition -> this
    is InteropLib.HeaderFile -> TODO()
}