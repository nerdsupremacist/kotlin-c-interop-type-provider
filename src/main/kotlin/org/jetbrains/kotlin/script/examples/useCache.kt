package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.daemon.common.toHexString
import java.io.File
import java.security.MessageDigest
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.SourceCode
import kotlin.script.experimental.api.hostConfiguration
import kotlin.script.experimental.host.ScriptingHostConfiguration
import kotlin.script.experimental.jvm.compilationCache
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.CompiledScriptJarsCache

fun ScriptCompilationConfiguration.Builder.useCache(cache: Cache) {
    hostConfiguration(ScriptingHostConfiguration {
        jvm {
            compilationCache(
                CompiledScriptJarsCache { script, scriptCompilationConfiguration ->
                    File(cache.compilerCacheDir, compiledScriptUniqueName(script, scriptCompilationConfiguration) + ".jar")
                }
            )
        }
    })
}

private fun compiledScriptUniqueName(script: SourceCode, scriptCompilationConfiguration: ScriptCompilationConfiguration): String {
    val digestWrapper = MessageDigest.getInstance("MD5")
    digestWrapper.update(script.text.toByteArray())
    scriptCompilationConfiguration.notTransientData.entries
        .sortedBy { it.key.name }
        .forEach {
            digestWrapper.update(it.key.name.toByteArray())
            digestWrapper.update(it.value.toString().toByteArray())
        }
    return digestWrapper.digest().toHexString()
}