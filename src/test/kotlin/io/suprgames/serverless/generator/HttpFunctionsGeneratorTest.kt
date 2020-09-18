package io.suprgames.serverless.generator

import io.suprgames.serverless.HttpFunction
import io.suprgames.serverless.HttpMethod
import com.amazonaws.services.lambda.runtime.Context;
import org.junit.Test
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.*
import java.io.StringReader
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

@HttpFunction("", HttpMethod.GET, "example1")
class HttpHandlerClassExample {

}

class ClassWith2HttpHandlersMethod {

    @HttpFunction("", HttpMethod.GET, "example2/method1")
    fun method1(input: Map<String, Any>, context: Context) {

    }

    @HttpFunction("", HttpMethod.POST, "example2/method2")
    fun method2(input: Map<String, Any>, context: Context) {

    }
}

fun Node.nodeByName(name: String): NodeTuple =
        (this as MappingNode).value.find { (it.keyNode as ScalarNode).value == name }!!


class HttpFunctionsGeneratorTest {

    @Test
    fun `The generated HTTP functions should be like the expected`() {
        val mappingNode = (Yaml().compose(StringReader(httpFunctions(reflections).toString())) as MappingNode)
        mappingNode.nodeByName("http-handler-class-example").let { handler ->
            assertEquals("io.suprgames.serverless.generator.HttpHandlerClassExample", (handler.valueNode.nodeByName("handler").valueNode as ScalarNode).value)
            (handler.valueNode.nodeByName("events").valueNode as CollectionNode<*>).value.let { events ->
                assertEquals(1, events.size)
                (events[0] as Node).nodeByName("http").valueNode.let { http ->
                    assertEquals("example1", (http.nodeByName("path").valueNode as ScalarNode).value)
                    assertEquals("get", (http.nodeByName("method").valueNode as ScalarNode).value)
                    assertEquals("true", (http.nodeByName("cors").valueNode as ScalarNode).value)
                }
            }
        }
        mappingNode.nodeByName("class-with2-http-handlers-method-method1").let { handler ->
            assertEquals("io.suprgames.serverless.generator.ClassWith2HttpHandlersMethod::method1", (handler.valueNode.nodeByName("handler").valueNode as ScalarNode).value)
            (handler.valueNode.nodeByName("events").valueNode as CollectionNode<*>).value.let { events ->
                assertEquals(1, events.size)
                (events[0] as Node).nodeByName("http").valueNode.let { http ->
                    assertEquals("example2/method1", (http.nodeByName("path").valueNode as ScalarNode).value)
                    assertEquals("get", (http.nodeByName("method").valueNode as ScalarNode).value)
                    assertEquals("true", (http.nodeByName("cors").valueNode as ScalarNode).value)
                }
            }
        }
        mappingNode.nodeByName("class-with2-http-handlers-method-method2").let { handler ->
            assertEquals("io.suprgames.serverless.generator.ClassWith2HttpHandlersMethod::method2", (handler.valueNode.nodeByName("handler").valueNode as ScalarNode).value)
            (handler.valueNode.nodeByName("events").valueNode as CollectionNode<*>).value.let { events ->
                assertEquals(1, events.size)
                (events[0] as Node).nodeByName("http").valueNode.let { http ->
                    assertEquals("example2/method2", (http.nodeByName("path").valueNode as ScalarNode).value)
                    assertEquals("post", (http.nodeByName("method").valueNode as ScalarNode).value)
                    assertEquals("true", (http.nodeByName("cors").valueNode as ScalarNode).value)
                }
            }
        }
    }

}