package org.jetbrains.kotlin.script.examples.cache

import java.io.File

class NoopCache : Cache {
    override val path: File = createTempDir().apply { deleteOnExit() }

    override fun String.hasChanged(vararg data: String) = true
}