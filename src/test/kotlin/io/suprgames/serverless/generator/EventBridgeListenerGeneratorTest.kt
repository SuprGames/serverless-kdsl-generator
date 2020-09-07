package io.suprgames.serverless.generator

import io.suprgames.serverless.EventBridgeListener
import org.junit.Test
import kotlin.test.assertEquals

data class TestEvent1(val player: String, val amount: Long)

@EventBridgeListener(eventBusArn = "arnForEventBus", eventToListen = TestEvent1::class)
class TestEventBridgeListener1

class EventBridgeListenerGeneratorTest {

    @Test
    fun `The generated EventBridge listener should be like the expected`() {
        val expected = StringBuffer()
        expected.appendln("  test-event-bridge-listener1:")
        expected.appendln("    handler: io.suprgames.serverless.generator.TestEventBridgeListener1")
        expected.appendln("    events:")
        expected.appendln("      - eventBridge:")
        expected.appendln("          eventBus: arnForEventBus")
        expected.appendln("          pattern:")
        expected.appendln("            detail-type:")
        expected.appendln("              - 'io.suprgames.serverless.generator.TestEvent1'")
        assertEquals(expected.toString(), eventBridgeListeners(reflections).toString())
    }
}