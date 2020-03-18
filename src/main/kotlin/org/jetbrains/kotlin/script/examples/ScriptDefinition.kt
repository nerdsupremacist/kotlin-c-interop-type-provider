package org.jetbrains.kotlin.script.examples

import org.jetbrains.kotlin.script.examples.cache.GeneralCache
import org.jetbrains.kotlin.script.examples.cache.NoopCache
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

object ScriptDefinition : ScriptCompilationConfiguration({
    val cache = GeneralCache.current()

    defaultImports(
        File::class,
        Include::class
    )

    jvm {
        dependenciesFromClassContext(ScriptDefinition::class, wholeClasspath = true)
    }

    refineConfiguration {
        compilerOptions.append("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
        onAnnotations(Include::class, handler = Configurator(cache = cache ?: NoopCache()))
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    // TODO: Figure out how to still run the refinement while caching the individual scripts
//    cache?.let(::useCache)
})