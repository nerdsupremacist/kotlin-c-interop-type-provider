package org.jetbrains.kotlin.script.examples.interop

import java.io.File
import kotlin.script.experimental.api.SourceCode

sealed class InteropLib {
    class Definition(val file: File) : InteropLib()
    class HeaderFile(val file: File) : InteropLib()
}

fun InteropLib.Definition.sourceCode(): SourceCode {
    TODO()
}

fun InteropLib.definition(): InteropLib.Definition = when (this) {
    is InteropLib.Definition -> this
    is InteropLib.HeaderFile -> TODO()
}