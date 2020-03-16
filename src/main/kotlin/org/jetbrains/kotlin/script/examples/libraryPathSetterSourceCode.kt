package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.script.examples.cache.Cache
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.host.toScriptSource

// This is required to set the path of the .dylib, .dll, etc. files
internal fun libraryPathSetterSourceCode(cache: Cache): SourceCode {
    val setter = """
        System.setProperty("java.library.path", "${cache.path.absolutePath}:" + System.getProperty("java.library.path"))
        ClassLoader::class.java.getDeclaredField("sys_paths").run { 
            isAccessible = true
            set(null, null)
            isAccessible = false
        }
    """.trimIndent()

    return with(cache) {
        "libraryPathSetterSourceCode.$extension.kts"
            .file()
            .apply {
                if (!exists()) {
                    createNewFile()
                    writeText(setter)
                }
            }
            .toScriptSource()
    }
}