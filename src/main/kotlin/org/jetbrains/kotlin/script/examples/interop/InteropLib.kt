package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asErrorDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult

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
        stubs = File(parentFolder, "${file.nameWithoutExtension}/${file.nameWithoutExtension}.kt"),
        jars = listOf(nativeJar)
    )
}

fun InteropLib.definition(): ResultWithDiagnostics<InteropLib.Definition> = when (this) {
    is InteropLib.Definition -> this.asSuccess()
    is InteropLib.HeaderFile ->
        /*
            TODO: We need to
                1. Locate the implementation
                2. Compile the implementation
                3. Create a def file for it
         */
        makeFailureResult("Including a header file directly is not implemented yet. Please use a .def file".asErrorDiagnostics())
}