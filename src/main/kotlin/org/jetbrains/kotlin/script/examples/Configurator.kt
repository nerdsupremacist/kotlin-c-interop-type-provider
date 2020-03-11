package org.jetbrains.kotlin.script.examples

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

        val definitions = annotations
            .mapSuccess { it.lib(baseDirectory) }
            .valueOr { return it }
            .mapSuccess { it.definition() }
            .valueOr { return it }

        val libraries = runBlocking {
            definitions.map { definition ->
                async {
                    definition.library()
                }
            }.awaitAll()
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