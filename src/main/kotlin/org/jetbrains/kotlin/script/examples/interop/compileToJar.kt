package org.jetbrains.kotlin.script.examples.interop

import org.jetbrains.kotlin.script.examples.cache.Cache
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.saveToJar

private val compiler = JvmScriptCompiler()

suspend fun File.compileToJar(
    cache: Cache,
    compilationConfiguration: ScriptCompilationConfiguration
): ResultWithDiagnostics<File> = cache
    .run {
        generates("$nameWithoutExtension.jar")
    }
    .ifMissed { jarFile ->
        val refinedCompilationConfiguration = compilationConfiguration.with {
            importScripts(toScriptSource())
        }

        val dummyScript = "${nameWithoutExtension}_dummyScript.c.kts".file().apply { createNewFile() }
        val compiled = compiler(dummyScript.toScriptSource(), refinedCompilationConfiguration)
            .valueOr { return it } as KJvmCompiledScript<*>

        compiled.saveToJar(jarFile)
    }
    .value
    .asSuccess()