package org.jetbrains.kotlin.script.examples

import java.io.File

const val COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR = "KOTLIN_${name}_KTS_COMPILED_SCRIPTS_CACHE_DIR"
const val COMPILED_SCRIPTS_CACHE_DIR_PROPERTY = "kotlin.${extension}.kts.compiled.scripts.cache.dir"

data class Cache(
    val compilerCacheDir: File,
    val typeProviderCacheDir: File
) {

    companion object {
        fun current(): Cache? {
            val cacheExtSetting = System.getProperty(COMPILED_SCRIPTS_CACHE_DIR_PROPERTY)
                ?: System.getenv(COMPILED_SCRIPTS_CACHE_DIR_ENV_VAR)

            val cacheBaseDir = when {
                cacheExtSetting == null -> System.getProperty("java.io.tmpdir")
                    ?.let(::File)?.takeIf { it.exists() && it.isDirectory }
                    ?.let { File(it, "main.kts.compiled.cache").apply { mkdir() } }
                cacheExtSetting.isBlank() -> null
                else -> File(cacheExtSetting)
            }?.takeIf { it.exists() && it.isDirectory } ?: return null

            return Cache(
                compilerCacheDir = cacheBaseDir,
                typeProviderCacheDir = File(cacheBaseDir, "${name}TypeProviderCodeGen").apply { mkdirs() }
            )
        }
    }
}