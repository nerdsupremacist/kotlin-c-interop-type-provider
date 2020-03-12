package org.jetbrains.kotlin.script.examples

import org.junit.Assert
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

class ScriptTest {

    @Test
    fun `Sample Test`() {
        val (res, out) = captureOut { evalFile("hello-world") }
        val lines = out.lines()

        assertSucceeded(res)
        assertNoThrow(res)

        Assert.assertEquals("hello world", lines[0])
        Assert.assertEquals("erf(π) = 0.9999911238536323", lines[1])
    }

}

private fun assertSucceeded(res: ResultWithDiagnostics<EvaluationResult>) {
    Assert.assertTrue(
        "test failed:\n  ${res.reports.joinToString("\n  ") { it.message + if (it.exception == null) "" else ": ${it.exception}" }}",
        res is ResultWithDiagnostics.Success
    )
}

private fun assertNoThrow(res: ResultWithDiagnostics<EvaluationResult>) {
    val returnValue = res
        .valueOrNull()
        ?.returnValue ?: return

    val exception = when (returnValue) {
        is ResultValue.Error -> returnValue.error
        else -> return
    }

    val failureMessage = """
        Script threw unexpected error:
            $exception
            Message: ${exception.message}
            Stacktrace:
    """.trimIndent() + "\n" + exception.stackTrace.joinToString("\n").prependIndent().prependIndent()

    Assert.fail(failureMessage)
}

private fun evalFile(scriptName: String): ResultWithDiagnostics<EvaluationResult> {
    val scriptFile = File("testData/$scriptName.$extension.kts")
    val scriptDefinition = createJvmCompilationConfigurationFromTemplate<Script>()

    val evaluationEnv = ScriptEvaluationConfiguration {
        jvm {
            baseClassLoader(null)
        }
        constructorArgs(emptyArray<String>())
        enableScriptsInstancesSharing()
    }

    return BasicJvmScriptingHost().eval(scriptFile.toScriptSource(), scriptDefinition, evaluationEnv)
}

private fun <T> captureOutResult(body: () -> T): Pair<Result<T>, String> {
    val outStream = ByteArrayOutputStream()
    val prevOut = System.out
    System.setOut(PrintStream(outStream))

    val result = runCatching { body() }

    System.out.flush()
    System.setOut(prevOut)

    return result to outStream.toString().trim()
}

private fun <T> captureOut(body: () -> T): Pair<T, String> {
    return captureOutResult { body() }.run { first.getOrThrow() to second }
}