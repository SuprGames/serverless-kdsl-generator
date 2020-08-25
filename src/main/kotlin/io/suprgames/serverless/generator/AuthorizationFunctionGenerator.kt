package io.suprgames.serverless.generator

import io.suprgames.serverless.AuthorizerFunction
import org.reflections.Reflections

/**
 * This is how should look a HttpFunction entry
 *   playerAuthorizationCheck:
 *     handler: io.suprgames.operatorapi.player.AuthorizationCheck
 */
fun authorizerFunctions(reflections: Reflections): StringBuffer =
        reflections.getTypesAnnotatedWith(AuthorizerFunction::class.java)
                .let { classesWithAnnotation ->
                    val stringBuffer = StringBuffer()
                    println("      Generating [${classesWithAnnotation.size}] Token Authorizer functions...")
                    classesWithAnnotation.forEach { annotatedClass ->
                        println("        ${annotatedClass.simpleName}")
                        val annotation = annotatedClass.getAnnotation(AuthorizerFunction::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, annotatedClass.simpleName)}:")
                        stringBuffer.appendln("    handler: ${annotatedClass.name}")
                    }
                    stringBuffer
                }
