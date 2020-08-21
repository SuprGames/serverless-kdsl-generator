package io.suprgames.serverless.generator

import io.suprgames.serverless.WebSocketConnector
import org.reflections.Reflections

/**
 * This is how should look a WebSocketFunction entry
 *   websocket-disconnect:
 *     handler: io.suprgames.connector.handlers.DisconnectHandler
 *     events:
 *       - websocket:
 *           route: $disconnect
 */
fun websocketConnectors(reflections: Reflections): StringBuffer =
        reflections.getTypesAnnotatedWith(WebSocketConnector::class.java)
                .let {
                    val stringBuffer = StringBuffer()
                    println("      Generating [${it.size}] WebSocket connectors...")
                    it.forEach {
                        println("        ${it.simpleName}")
                        val annotation = it.getAnnotation(WebSocketConnector::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, it.simpleName)}:")
                        stringBuffer.appendln("    handler: ${it.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - websocket:")
                        stringBuffer.appendln("          route: ${annotation.route}")
                    }
                    stringBuffer
                }
