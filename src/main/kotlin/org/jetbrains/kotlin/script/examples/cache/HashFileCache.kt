package org.jetbrains.kotlin.script.examples.cache

import org.jetbrains.kotlin.daemon.common.toHexString
import java.io.File
import java.security.MessageDigest
import java.util.*

class HashFileCache(override val path: File) : Cache {

    private val propertiesFile = File(path, "cachedFiles").apply { createNewFile() }
    private val properties by lazy { Properties().apply { load(propertiesFile.inputStream()) } }

    private fun setHash(file: File, hash: String) {
        properties.setProperty(file.absolutePath, hash)
        properties.store(propertiesFile.outputStream(), null)
    }

    override fun File.hasChanged(vararg additionalData: String): Boolean {
        val hash = hash(*additionalData)
        val previousHash = properties.getProperty(absolutePath)

        return (hash != previousHash).also { hasChanged ->
            if (hasChanged) setHash(this, hash)
        }
    }
}

private fun File.hash(vararg additionalData: String): String {
    val digestWrapper = MessageDigest.getInstance("MD5")
    digestWrapper.update(readBytes())
    additionalData.forEach { digestWrapper.update(it.toByteArray()) }
    return digestWrapper.digest().toHexString()
}