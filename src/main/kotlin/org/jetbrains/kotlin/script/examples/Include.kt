package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.script.examples.interop.IncludedInterop
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult

//region Include Annotation used in Script

@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Include(
    val path: String,
    val name: String = "",
    val compilerOptions: String = "",
    val linkerOptions: String = ""
)

//endregion


//region Resolve the type of included interop

internal fun Include.resolve(baseDirectory: File?): ResultWithDiagnostics<IncludedInterop> {
    val file = baseDirectory?.resolve(path)?.takeIf { it.exists() } ?: File(path)

    return when (file.extension) {
        "def" -> if (compilerOptions.isBlank() && linkerOptions.isBlank())
            IncludedInterop.Definition(file).asSuccess()
        else
            makeFailureResult("Importing a def file with additional compiler or linker options is not supported")

        "h" -> IncludedInterop.HeaderFile(
            path = path,
            name = name.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension,
            compilerOptions = compilerOptions.split(" ").filter { it.isNotBlank() },
            linkerOptions = linkerOptions.split(" ").filter { it.isNotBlank() },
            baseDirectory = baseDirectory
        ).asSuccess()

        else -> return makeFailureResult("File $file cannot be included")
    }
}

//endregion