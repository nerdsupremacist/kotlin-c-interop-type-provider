package org.jetbrains.kotlin.script.examples

import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.asSuccess
import kotlin.script.experimental.api.valueOr

fun <I, O> ResultWithDiagnostics<I>.map(transform: (I) -> O): ResultWithDiagnostics<O> =
    valueOr { return it }.let(transform).asSuccess()