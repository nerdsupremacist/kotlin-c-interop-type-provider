package org.jetbrains.kotlin.script.examples

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.mapSuccess

internal suspend fun <T, O> Iterable<T>.parallelMapSuccess(block: suspend (T) -> ResultWithDiagnostics<O>) = coroutineScope {
    map { element ->
        async {
            block(element)
        }
    }.awaitAll().mapSuccess { it }
}