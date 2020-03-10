package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi

sealed class InteropLib {
    class Definition(val file: File) : InteropLib()
    class HeaderFile(val file: File) : InteropLib()
}

@ExperimentalCoroutinesApi
suspend fun InteropLib.Definition.library(): Library {
    val parentFolder = createTempDir("CInterOp", suffix = "")
        .also { it.mkdirs() }
        .also { it.deleteOnExit() }

    val copied = File(parentFolder, file.name).also { it.deleteOnExit() }
    file.copyTo(copied)

    val command =  "cinterop -def ${copied.absolutePath} -o ${parentFolder.absolutePath}/lib"
    shell(dir = parentFolder) {
       command()
    }

    return Library(
        klib = File(parentFolder, "lib.klib"),
        stubs = File(parentFolder, "lib-build/kotlin/${file.nameWithoutExtension}/${file.nameWithoutExtension}.kt")
    )
}

fun InteropLib.definition(): InteropLib.Definition = when (this) {
    is InteropLib.Definition -> this
    is InteropLib.HeaderFile -> TODO()
}