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

    val nativeFolder = File("native")
    val processCLib = File(nativeFolder, "bin/processCLib")
    val nativeJar = File(nativeFolder, "konan/lib/kotlin-native.jar")

    val command = "${processCLib.absolutePath} -def ${file.absolutePath} -no-default-libs -Xpurge-user-libs -mode sourcecode -no-endorsed-libs"
    shell(dir = parentFolder) {
       command()
    }

    return Library(
        name = file.nameWithoutExtension,
        klib = File(parentFolder, "lib.klib"),
        stubs = File(parentFolder, "${file.nameWithoutExtension}/${file.nameWithoutExtension}.kt"),
        jars = listOf(nativeJar)
    )
}

fun InteropLib.definition(): InteropLib.Definition = when (this) {
    is InteropLib.Definition -> this
    is InteropLib.HeaderFile -> TODO()
}