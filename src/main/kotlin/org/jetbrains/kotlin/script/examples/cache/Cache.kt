package org.jetbrains.kotlin.script.examples.cache

import java.io.File

interface Cache {
    val path: File

    fun String.file(): File = File(path, this)
    fun File.hasChanged(vararg additionalData: String): Boolean

    fun File.generates(path: String, vararg additionalData: String): CachedResult<File> {
        val file = path.file()
        return if (file.exists() && !hasChanged()) {
            CachedResult.Cached(this@Cache, file)
        } else {
            CachedResult.Missed(this@Cache, file)
        }
    }

    fun File?.generatesIfExists(path: String, vararg additionalData: String): CachedResult<File> =
        this?.generates(path, *additionalData) ?: CachedResult.Missed(this@Cache, path.file())
}

