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
                .let { classesWithAnnotation ->
                    val stringBuffer = StringBuffer()
                    println("      Generating [${classesWithAnnotation.size}] WebSocket connectors...")
                    classesWithAnnotation.forEach { annotatedClass ->
                        println("        ${annotatedClass.simpleName}")
                        val annotation = annotatedClass.getAnnotation(WebSocketConnector::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, annotatedClass.simpleName)}:")
                        stringBuffer.appendln("    handler: ${annotatedClass.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - websocket:")
                        stringBuffer.appendln("          route: ${annotation.route}")
                    }
                    stringBuffer
                }
