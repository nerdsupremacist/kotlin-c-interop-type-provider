package org.jetbrains.kotlin.script.examples.interop

import java.io.File

data class Library(val name: String, val stubs: File, val jars: List<File>)