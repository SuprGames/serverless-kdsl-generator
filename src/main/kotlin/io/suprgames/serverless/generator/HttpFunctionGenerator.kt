package io.suprgames.serverless.generator

import io.suprgames.serverless.HttpFunction
import io.suprgames.serverless.SqsConsumer
import org.reflections.Reflections

/**
 * This is how should look a HttpFunction entry
 *   registerPlayer:
 *     handler: io.suprgames.operatorapi.player.RegisterPlayerHandler
 *     events:
 *       - http:
 *           path: player/register
 *           method: post
 */
fun httpFunctions(reflections: Reflections): StringBuffer =
        reflections.getTypesAnnotatedWith(HttpFunction::class.java)
                .let {
                    val stringBuffer = StringBuffer()
                    println("      Generating [${it.size}] HTTP functions...")
                    it.forEach {
                        println("        ${it.simpleName}")
                        val annotation = it.getAnnotation(HttpFunction::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, it.simpleName)}:")
                        stringBuffer.appendln("    handler: ${it.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - http:")
                        stringBuffer.appendln("          path: ${annotation.path}")
                        stringBuffer.appendln("          method: ${annotation.method.toString().toLowerCase()}")
                    }
                    stringBuffer
                }

