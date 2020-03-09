package org.jetbrains.kotlin.script.examples

import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

object ScriptDefinition : ScriptCompilationConfiguration({
    // TODO: Import anything that your generated code might use

    defaultImports(
        File::class,
        Include::class
    )

    jvm {
        dependenciesFromClassContext(ScriptDefinition::class, wholeClasspath = true)
    }

    refineConfiguration {
        onAnnotations(Include::class, handler = Configurator)
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }
})