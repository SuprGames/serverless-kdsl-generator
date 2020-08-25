package io.suprgames.serverless.generator

import io.suprgames.serverless.EventBridgeListener
import org.reflections.Reflections

/**
 * This is how should look a EventBridgeListenerFunction entry
 *   gameActivationListener:
 *     handler: io.suprgames.listener.GameActivationListener
 *     events:
 *       - eventBridge:
 *           eventBus: ${self:provider.environment.eventBusArn}
 *           pattern:
 *             detail-type:
 *               - 'io.suprgames.connector.handlers.GameActivationHandler.GameActivationRequestedEvent'
 */
fun eventBridgeListeners(reflections: Reflections): StringBuffer =
        reflections.getTypesAnnotatedWith(EventBridgeListener::class.java)
                .let { classesWithAnnotation ->
                    val stringBuffer = StringBuffer()
                    println("      Generating [${classesWithAnnotation.size}] Event Bridge Listeners...")
                    classesWithAnnotation.forEach { annotatedClass ->
                        println("        ${annotatedClass.simpleName}")
                        val annotation = annotatedClass.getAnnotation(EventBridgeListener::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, annotatedClass.simpleName)}:")
                        stringBuffer.appendln("    handler: ${annotatedClass.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - eventBridge:")
                        stringBuffer.appendln("          eventBus: ${annotation.eventBusArn}")
                        stringBuffer.appendln("          pattern:")
                        stringBuffer.appendln("            detail-type:")
                        stringBuffer.appendln("              - '${annotation.eventToListen.qualifiedName}'")
                    }
                    stringBuffer
                }


