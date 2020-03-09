package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.script.examples.interop.definition
import org.jetbrains.kotlin.script.examples.interop.sourceCode
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileBasedScriptSource

object Configurator : RefineScriptCompilationConfigurationHandler {

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
            .valueOr { return it }
            .map { it.definition() }
            .map { it.sourceCode() }

        return context
            .compilationConfiguration
            .with {
                importScripts.append(generatedScripts)
            }
            .asSuccess()
    }

}