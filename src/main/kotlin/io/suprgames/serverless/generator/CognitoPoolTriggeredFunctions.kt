package io.suprgames.serverless.generator

import io.suprgames.serverless.CognitoUserPoolTriggered
import org.reflections.Reflections

/**
 * Will generate something like:
 *
 *   user-registration-triggered:
 *     handler: io.suprgames.triggered.UserRegistrationTriggered
 *      events:
 *        - cognitoUserPool:
 *            pool: suprgames-userpool
 *            trigger: PostConfirmation
 *            existing: true
 *
 */
fun cognitoPoolTriggeredFunctions(reflections: Reflections): StringBuffer =
        reflections.getTypesAnnotatedWith(CognitoUserPoolTriggered::class.java)
                .let { classesWithAnnotation ->
                    val stringBuffer = StringBuffer()
                    println("      Generating [${classesWithAnnotation.size}] Cognito user pool triggered...")
                    classesWithAnnotation.forEach { annotatedClass ->
                        println("        ${annotatedClass.simpleName}")
                        val annotation = annotatedClass.getAnnotation(CognitoUserPoolTriggered::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, annotatedClass.simpleName)}:")
                        stringBuffer.appendln("    handler: ${annotatedClass.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - cognitoUserPool:")
                        stringBuffer.appendln("          pool: ${annotation.pool}")
                        stringBuffer.appendln("          trigger: ${annotation.trigger}")
                        stringBuffer.appendln("          existing: ${annotation.existing}")
                    }
                    stringBuffer
                }
