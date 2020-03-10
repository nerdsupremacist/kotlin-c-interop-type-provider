package org.jetbrains.kotlin.script.examples

import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

object ScriptDefinition : ScriptCompilationConfiguration({
    defaultImports(
        File::class,
        Include::class
    )

    jvm {
        dependenciesFromClassContext(ScriptDefinition::class, wholeClasspath = true)
    }

    refineConfiguration {
        compilerOptions.append("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
        onAnnotations(Include::class, handler = Configurator)
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
})