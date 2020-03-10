package org.jetbrains.kotlin.script.examples

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.script.examples.interop.definition
import org.jetbrains.kotlin.script.examples.interop.library
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.updateClasspath

object Configurator : RefineScriptCompilationConfigurationHandler {

    @ExperimentalCoroutinesApi
    override fun invoke(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
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
            .mapSuccess { it.lib(baseDirectory) }
            .valueOr { return@invoke it }
            .map { it.definition() }
            .let { definitions ->
                runBlocking {
                    definitions.map { it.library() }
                }
            }

        val imports = libraries.map { "${it.name}.*" }
        val scripts = libraries.map { it.stubs.toScriptSource() }
        val jars = libraries.flatMap { it.jars }.distinct()

        return context
            .compilationConfiguration
            .with {
                defaultImports.append(imports)
                importScripts.append(scripts)
                updateClasspath(jars)
            }
            .asSuccess()
    }

}