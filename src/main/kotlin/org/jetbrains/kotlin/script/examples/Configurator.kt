package org.jetbrains.kotlin.script.examples

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.script.examples.interop.library
import org.jetbrains.kotlin.script.examples.interop.toDefinition
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.updateClasspath

class Configurator(private val libraryFolder: File) : RefineScriptCompilationConfigurationHandler {
    private val libraryPathScript by lazy { libraryPathSetterSourceCode(libraryFolder) }

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
            .parallelMapSuccess { it.toDefinition(libraryFolder = libraryFolder) }
            .valueOr { return it }
            .parallelMapSuccess { it.library(libraryFolder = libraryFolder) }
            .valueOr { return it  }

        val imports = libraries.map { "${it.packageName.name}.*" }
        val scripts = libraries.map { it.stubs.toScriptSource() }
        val jars = libraries.flatMap { it.jars }.distinct()

        return context
            .compilationConfiguration
            .with {
                defaultImports.append(imports)
                importScripts.append(libraryPathScript)
                importScripts.append(scripts)
                updateClasspath(jars)
            }
            .asSuccess()
    }


}