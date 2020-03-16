package org.jetbrains.kotlin.script.examples.cache

import org.jetbrains.kotlin.daemon.common.toHexString
import java.io.File
import java.security.MessageDigest
import java.util.*

class HashFileCache(override val path: File) : Cache {
    private val propertiesFile = File(path, "cachedFiles").apply { createNewFile() }
    private val properties by lazy { Properties().apply { load(propertiesFile.inputStream()) } }

    private fun String.setHash(hash: String) {
        properties.setProperty(this, hash)
        properties.store(propertiesFile.outputStream(), null)
    }

    override fun String.hasChanged(vararg data: String): Boolean {
        val hash = hash(*data)
        val previousHash = properties.getProperty(this)
        return (hash != previousHash).also { hasChanged ->
            if (hasChanged) setHash(hash)
        }
    }
}

private fun hash(vararg data: String): String {
    val digestWrapper = MessageDigest.getInstance("MD5")
    data.forEach { digestWrapper.update(it.toByteArray()) }
    return digestWrapper.digest().toHexString()
}