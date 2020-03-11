package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.script.examples.interop.InteropLib
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult


@Target(AnnotationTarget.FILE)
@Repeatable
@Retention(AnnotationRetention.SOURCE)
annotation class Include(val path: String)

fun Include.lib(baseDirectory: File?): ResultWithDiagnostics<InteropLib> {
    val file = baseDirectory?.resolve(path)?.takeIf { it.exists() }
        ?: File(path).takeIf { it.exists() }
        ?: return makeFailureResult("File for $path cannot be found")

    return when (file.extension) {
        "def" -> InteropLib.Definition(file).asSuccess()
        "h" -> InteropLib.HeaderFile(file).asSuccess()
        else -> return makeFailureResult("File $file cannot be included")
    }
}