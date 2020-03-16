package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.kotlin.script.examples.cache.Cache
import org.jetbrains.kotlin.script.examples.map
import java.util.*
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult
import kotlin.script.experimental.api.valueOr

//region The Included Interop C Library

internal sealed class IncludedInterop {
    class Definition(val file: File) : IncludedInterop()
    class HeaderFile(
        val path: String,
        val name: String,
        val compilerOptions: List<String>,
        val linkerOptions: List<String>,
        val baseDirectory: File?
    ) : IncludedInterop()
}

//endregion


//region Converting the Included Library to the Definition with a proper .def file

@ExperimentalCoroutinesApi
internal suspend fun IncludedInterop.toDefinition(cache: Cache) = when (this) {
    is IncludedInterop.Definition -> this.asSuccess()
    is IncludedInterop.HeaderFile -> definitionFile(cache = cache).map(IncludedInterop::Definition)
}

@ExperimentalCoroutinesApi
private suspend fun IncludedInterop.HeaderFile.definitionFile(
    cache: Cache
): ResultWithDiagnostics<File> {
    val linkerOptions = linkerOptions.toMutableList()
    val localFile = baseDirectory?.resolve(path)?.takeIf { it.exists() }

    // If this is a header file that exists, then we need to compile it
    val binary = localFile
        ?.run {
            compileWithClang(
                binaryName = nameWithoutExtension,
                cache = cache,
                compilerOptions = compilerOptions,
                linkerOptions = linkerOptions
            )
        }
        ?.valueOr { return it }
        ?.also { linkerOptions.add(it.absolutePath) }

    return cache
        .run {
            binary?.generates("$name.def", *compilerOptions.toTypedArray(), *linkerOptions.toTypedArray())
                ?: "${name}Definition".generates("$name.def", *compilerOptions.toTypedArray(), *linkerOptions.toTypedArray())
        }
        .ifMissed { defFile ->
            defFile.createNewFile()

            val properties = Properties()
            properties.setProperty("headers", localFile?.absolutePath ?: this@definitionFile.path)

            if (compilerOptions.isNotEmpty()) {
                properties.setProperty("compilerOpts", compilerOptions.joinToString(" "))
            }

            if (linkerOptions.isNotEmpty()) {
                properties.setProperty("linkerOpts", linkerOptions.joinToString(" "))
            }

            properties.store(defFile.outputStream(), null)
        }
        .value
        .asSuccess()
}

//endregion


//region Loading Information about the Library

internal fun IncludedInterop.Definition.info(): LibraryInfo {
    val properties = Properties().apply { load(file.inputStream()) }

    val packageName = properties
        .getProperty("package")
        ?.let { PackageName(it.split(".")) } ?: PackageName(listOf(file.nameWithoutExtension))

    return LibraryInfo(
        packageName = packageName,
        language = properties.getProperty("language")
    )
}

//endregion


//region Loading library from Definition

@ExperimentalCoroutinesApi
internal suspend fun IncludedInterop.Definition.library(cache: Cache): ResultWithDiagnostics<Library> {
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

    return cache
        .run {
            file.generates("${info.packageName.folder}/${file.nameWithoutExtension}.kt")
        }
        .ifMissed {
            val command = "${processCLib.absolutePath} -def ${file.absolutePath} -no-default-libs -Xpurge-user-libs -mode sourcecode -no-endorsed-libs"
            shell(dir = cache.path) {
                command()
            }
        }
        .value
        .let { stubs ->
            Library(
                packageName = info.packageName,
                stubs = stubs,
                jars = listOf(nativeJar)
            )
        }
        .asSuccess()
}

//endregion
