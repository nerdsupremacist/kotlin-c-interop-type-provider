package org.jetbrains.kotlin.script.examples

import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

object ScriptDefinition : ScriptCompilationConfiguration({
    val cache = Cache.current()

    val folder = cache?.typeProviderCacheDir ?: createTempDir("CInterop", "")
        .apply { deleteOnExit() }
        .apply { mkdirs() }

    defaultImports(
        File::class,
        Include::class
    )

    jvm {
        dependenciesFromClassContext(ScriptDefinition::class, wholeClasspath = true)
    }

    refineConfiguration {
        compilerOptions.append("-Xopt-in=kotlin.ExperimentalUnsignedTypes")
        onAnnotations(Include::class, handler = Configurator(libraryFolder = folder))
    }

    ide {
        acceptedLocations(ScriptAcceptedLocation.Everywhere)
    }

    cache?.let(::useCache)
})