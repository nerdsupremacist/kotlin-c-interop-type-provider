package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.*
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult

sealed class IncludedInterop {
    class Definition(val file: File) : IncludedInterop()
    class HeaderFile(
        val path: String,
        val name: String,
        val compilerOpts: List<String>,
        val linkerOpts: List<String>,
        val baseDirectory: File?
    ) : IncludedInterop()
}

@ExperimentalCoroutinesApi
suspend fun IncludedInterop.Definition.library(libraryFolder: File): ResultWithDiagnostics<Library> {
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

fun IncludedInterop.Definition.info(): LibraryInfo {
    val properties = Properties().apply { load(file.inputStream()) }

    val packageName = properties
        .getProperty("package")
        ?.let { PackageName(it.split(".")) } ?: PackageName(listOf(file.nameWithoutExtension))

    return LibraryInfo(
        packageName = packageName,
        language = properties.getProperty("language")
    )
}

@ExperimentalCoroutinesApi
suspend fun IncludedInterop.toDefinition(libraryFolder: File): ResultWithDiagnostics<IncludedInterop.Definition> {
    return when (this) {
        is IncludedInterop.Definition -> this.asSuccess()
        is IncludedInterop.HeaderFile -> {
            val linkerOptions = linkerOpts.toMutableList()
            val file = baseDirectory?.resolve(path)?.takeIf { it.exists() }

            // If this is a header file that exists, then we need to compile it
            if (file != null) {
                // Find the Implementation
                val implementation = File(
                    file.parentFile,
                    file.nameWithoutExtension + ".c"
                ).takeIf { it.exists() } ?: return makeFailureResult ("No Implementations for $path exist")

                // Determine where the binary should be
                val binary = File(libraryFolder, "$name.o")

                // Compile that with compiler and linker args
                shell(dir = implementation.parentFile) {
                    val options = (compilerOpts + linkerOptions).joinToString(" ")
                    "clang -c -o ${binary.absolutePath} ${implementation.absolutePath} $options"()
                }

                // Link new binary with the generated stubs
                linkerOptions.add(binary.absolutePath)
            }

            // Create Def File
            val defFile = File(baseDirectory, "$name.def")
                .apply { createNewFile() }
                .apply { deleteOnExit() }

            // Set Properties of the Def file
            val properties = Properties()
            properties.setProperty("headers", file?.absolutePath ?: path)

            if (compilerOpts.isNotEmpty()) {
                properties.setProperty("compilerOpts", compilerOpts.joinToString(" "))
            }

            if (linkerOptions.isNotEmpty()) {
                properties.setProperty("linkerOpts", linkerOptions.joinToString(" "))
            }

            properties.store(defFile.outputStream(), null)

            IncludedInterop.Definition(defFile).asSuccess()
        }
    }
}