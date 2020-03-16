package org.jetbrains.kotlin.script.examples

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.script.examples.cache.Cache
import org.jetbrains.kotlin.script.examples.interop.compileToJar
import org.jetbrains.kotlin.script.examples.interop.library
import org.jetbrains.kotlin.script.examples.interop.toDefinition
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.updateClasspath

class Configurator(private val cache: Cache) : RefineScriptCompilationConfigurationHandler {
    private val libraryPathScript by lazy { libraryPathSetterSourceCode(cache = cache) }

    @ExperimentalCoroutinesApi
    override fun invoke(context: ScriptConfigurationRefinementContext) = runBlocking { processAnnotations(context) }

    @ExperimentalCoroutinesApi
    suspend fun processAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
        val baseDirectory = (context.script as? FileBasedScriptSource)?.file?.parentFile

        val annotations = context
            .collectedData
            ?.get(ScriptCollectedData.foundAnnotations)
            ?.mapNotNull { annotation ->
                when (annotation) {
                    is Include -> annotation
                    else -> null
                }
            }
            ?.takeIf { it.isNotEmpty() } ?: return context.compilationConfiguration.asSuccess()

        val libraries = annotations
            .mapSuccess { it.resolve(baseDirectory) }
            .valueOr { return it }
            .parallelMapSuccess { it.toDefinition(cache = cache) }
            .valueOr { return it }
            .parallelMapSuccess { it.library(cache = cache) }
            .valueOr { return it  }


        val jars = libraries.flatMap { it.jars }.distinct()
        val stubsCompilationConfiguration = context
            .compilationConfiguration
            .with {
                updateClasspath(jars)
            }

        val stubs = libraries
            .parallelMapSuccess { it.stubs.compileToJar(cache, stubsCompilationConfiguration) }
            .valueOr { return it }

        val imports = libraries.map { "${it.packageName.name}.*" }

        return context
            .compilationConfiguration
            .with {
                defaultImports.append(imports)
                importScripts.append(libraryPathScript)
                updateClasspath(stubs)
            }
            .asSuccess()
    }

}