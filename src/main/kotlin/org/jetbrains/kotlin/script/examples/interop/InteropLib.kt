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

    val command =  "/usr/local/kotlin-native/bin/cinterop -def ${file.absolutePath} -o ${parentFolder.absolutePath}/lib -no-default-libs -Xpurge-user-libs -mode sourcecode -no-endorsed-libs"
    shell {
       command()
    }

    return Library(
        name = file.nameWithoutExtension,
        klib = File(parentFolder, "lib.klib"),
        stubs = File(parentFolder, "lib-build/kotlin/${file.nameWithoutExtension}/${file.nameWithoutExtension}.kt"),
        jars = listOf(File("/usr/local/kotlin-native/konan/lib/kotlin-native.jar"))
    )
}

fun InteropLib.definition(): InteropLib.Definition = when (this) {
    is InteropLib.Definition -> this
    is InteropLib.HeaderFile -> TODO()
}