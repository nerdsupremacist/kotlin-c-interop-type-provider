package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.script.examples.interop.IncludedInterop
import org.jetbrains.kotlin.script.util.Import
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult

@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Include(
    val path: String,
    val name: String = "",
    val compilerOpts: String = "",
    val linkerOpts: String = ""
)

fun Include.resolve(baseDirectory: File?): ResultWithDiagnostics<IncludedInterop> {
    val file = baseDirectory?.resolve(path)?.takeIf { it.exists() }
        ?: File(path)

    return when (file.extension) {
        "def" -> if (compilerOpts.isBlank() && linkerOpts.isBlank())
            IncludedInterop.Definition(file).asSuccess()
        else
            makeFailureResult("Importing a def file with additional compiler or linker args is not supported")

        "h" -> IncludedInterop.HeaderFile(
            path = path,
            name = name.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension,
            compilerOpts = compilerOpts.split(" ").filter { it.isNotBlank() },
            linkerOpts = linkerOpts.split(" ").filter { it.isNotBlank() },
            baseDirectory = baseDirectory
        ).asSuccess()

        else -> return makeFailureResult("File $file cannot be included")
    }
}