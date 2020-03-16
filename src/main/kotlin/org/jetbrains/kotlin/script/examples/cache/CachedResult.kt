package org.jetbrains.kotlin.script.examples.cache

sealed class CachedResult<T>(val cache: Cache, val value: T) {
    class Cached<T>(cache: Cache, value: T) : CachedResult<T>(cache, value)
    class Missed<T>(cache: Cache, value: T) : CachedResult<T>(cache, value)

    inline fun ifMissed(block: Cache.(T) -> Unit): Cached<T> = when (this) {
        is Cached -> this
        is Missed -> {
            cache.run { block(value) }
            Cached(cache, value)
        }
    }
}
