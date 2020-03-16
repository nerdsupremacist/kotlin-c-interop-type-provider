package org.jetbrains.kotlin.script.examples.cache

import org.jetbrains.kotlin.script.examples.extension
import org.jetbrains.kotlin.script.examples.name
import java.io.File

const val COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR = "KOTLIN_${name}_KTS_COMPILED_SCRIPTS_CACHE_DIR"
const val COMPILED_SCRIPTS_CACHE_DIR_PROPERTY = "kotlin.$extension.kts.compiled.scripts.cache.dir"

data class GeneralCache(
    val compilerCacheDir: File,
    val typeProviderCacheDir: File
) : Cache by HashFileCache(
    typeProviderCacheDir
) {
    companion object {
        fun current(): GeneralCache? {
            val cacheExtSetting = System.getProperty(COMPILED_SCRIPTS_CACHE_DIR_PROPERTY)
                ?: System.getenv(COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR)

            val cacheBaseDir = when {
                cacheExtSetting == null -> System.getProperty("java.io.tmpdir")
                    ?.let(::File)?.takeIf { it.exists() && it.isDirectory }
                    ?.let { File(it, "main.kts.compiled.cache").apply { mkdir() } }
                cacheExtSetting.isBlank() -> null
                else -> File(cacheExtSetting)
            }?.takeIf { it.exists() && it.isDirectory } ?: return null

            return GeneralCache(
                compilerCacheDir = cacheBaseDir,
                typeProviderCacheDir = File(
                    cacheBaseDir,
                    "${name}TypeProviderCodeGen"
                ).apply { mkdirs() }
            )
        }
    }
}