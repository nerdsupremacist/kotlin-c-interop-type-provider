package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asErrorDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult

sealed class InteropLib {
    class Definition(val file: File) : InteropLib()
    class HeaderFile(val file: File) : InteropLib()
}

@ExperimentalCoroutinesApi
suspend fun InteropLib.Definition.library(libraryFolder: File): ResultWithDiagnostics<Library> {
    val info = info()

    info
        .language
        ?.takeIf { it != "C" }
        ?.let { language ->
            return makeFailureResult("Error loading ${info.packageName.name}: Language $language is not Supported")
        }

    val nativeFolder = File("native")
    val processCLib = File(nativeFolder, "bin/processCLib")
    val nativeJar = File(nativeFolder, "konan/lib/kotlin-native.jar")

    val command = "${processCLib.absolutePath} -def ${file.absolutePath} -no-default-libs -Xpurge-user-libs -mode sourcecode -no-endorsed-libs"
    shell(dir = libraryFolder) {
       command()
    }

    return Library(
        packageName = info.packageName,
        stubs = File(libraryFolder, "${info.packageName.folder}/${file.nameWithoutExtension}.kt"),
        jars = listOf(nativeJar)
    ).asSuccess()
}

fun InteropLib.Definition.info(): LibraryInfo {
    val properties = Properties().apply { load(file.inputStream()) }

    val packageName = properties
        .getProperty("package")
        ?.let { PackageName(it.split(".")) } ?: PackageName(listOf(file.nameWithoutExtension))

    return LibraryInfo(
        packageName = packageName,
        language = properties.getProperty("language")
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
        makeFailureResult("Including a header file directly is not implemented yet. Please use a .def file")
}