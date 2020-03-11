package org.jetbrains.kotlin.script.examples.interop

import java.io.File

data class Library(val packageName: PackageName, val stubs: File, val libraryPath: File, val jars: List<File>)