package org.jetbrains.kotlin.script.examples.interop

import eu.jrie.jetbrains.kotlinshell.shell.shell
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.jetbrains.kotlin.script.examples.cache.Cache
import java.io.File
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.makeFailureResult

@ExperimentalCoroutinesApi
suspend fun File.compileWithClang(
    binaryName: String,
    cache: Cache,
    compilerOptions: List<String> = emptyList(),
    linkerOptions: List<String> = emptyList()
): ResultWithDiagnostics<File> {

    // Find the Implementation
    val implementation = when (extension) {
        "h" -> File(
            parentFile,
            "$nameWithoutExtension.c"
        ).takeIf { it.exists() } ?: return makeFailureResult("No Implementations for $path exist")
        "c" -> this
        else -> return makeFailureResult("Cannot compile unsupported file format $extension")
    }

    // Determine where the binary should be
    return cache
        .run {
            implementation.generates("$binaryName.o", *compilerOptions.toTypedArray(), *linkerOptions.toTypedArray())
        }
        .ifMissed { binary ->
            // Compile that with compiler and linker args
            shell(dir = implementation.parentFile) {
                val options = (compilerOptions + linkerOptions).joinToString(" ")
                "clang -c -o ${binary.absolutePath} ${implementation.absolutePath} $options"()
            }
        }
        .value
        .asSuccess()
}