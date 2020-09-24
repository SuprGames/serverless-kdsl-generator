package io.suprgames.serverless.generator

import io.suprgames.serverless.MinionFunction
import org.junit.Test
import kotlin.test.assertEquals

@MinionFunction
class ExampleMinionFunction

class MinionFunctionTest {

    @Test
    fun `The generated EventBridge listener should be like the expected`() {
        val expected = StringBuffer()
        expected.appendln("  example-minion-function:")
        expected.appendln("    handler: io.suprgames.serverless.generator.ExampleMinionFunction")
        val sb = ServerlessKDSLGenerator().generate(reflections, MinionFunction::class.java, MinionFunctionGenerator())
        assertEquals(expected.toString(), sb.toString())
    }

}