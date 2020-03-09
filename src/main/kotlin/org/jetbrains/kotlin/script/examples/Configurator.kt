package org.jetbrains.kotlin.script.examples

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.jetbrains.kotlin.script.examples.interop.definition
import org.jetbrains.kotlin.script.examples.interop.sourceCode
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource

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

        val generatedScripts = annotations
            .mapSuccess { it.lib(baseDirectory) }
            .valueOr { return@invoke it }
            .map { it.definition() }
            .let { definitions ->
                runBlocking {
                    definitions.map { it.sourceCode() }
                }
            }

        return context
            .compilationConfiguration
            .with {
                importScripts.append(generatedScripts)
            }
            .asSuccess()
    }

}