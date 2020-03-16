package org.jetbrains.kotlin.script.examples.cache

import java.io.File

interface Cache {
    val path: File

    fun String.file(): File = File(path, this)
    fun String.hasChanged(vararg data: String): Boolean

    fun File.hasChanged(vararg additionalData: String) = absolutePath.hasChanged(readText(), *additionalData)

    fun String.generates(path: String, vararg data: String): CachedResult<File> {
        val file = path.file()
        return if (file.exists() && !hasChanged(*data)) {
            CachedResult.Cached(this@Cache, file)
        } else {
            CachedResult.Missed(this@Cache, file)
        }
    }

    fun File.generates(path: String, vararg additionalData: String): CachedResult<File> {
        val file = path.file()
        return if (file.exists() && !hasChanged(*additionalData)) {
            CachedResult.Cached(this@Cache, file)
        } else {
            CachedResult.Missed(this@Cache, file)
        }
    }
}
