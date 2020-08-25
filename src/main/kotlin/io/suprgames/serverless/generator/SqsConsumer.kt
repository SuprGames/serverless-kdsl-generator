package io.suprgames.serverless.generator

import io.suprgames.serverless.SqsConsumer
import org.reflections.Reflections

/**
 * This is how should look a SqsConsumerFunction entry
 *   registerEvent:
 *     handler: src/Registrar.handler
 *     events:
 *       - sqs:
 *           arn: arn:aws:sqs:${self:provider.region}:#{AWS::AccountId}:${self:custom.eventQueueName}
 */
fun sqsConsumers(reflections: Reflections): StringBuffer =
        reflections.getTypesAnnotatedWith(SqsConsumer::class.java)
                .let { classesWithAnnotation ->
                    val stringBuffer = StringBuffer()
                    println("      Generating [${classesWithAnnotation.size}] Sqs consumers...")
                    classesWithAnnotation.forEach { annotatedClass ->
                        println("        ${annotatedClass.simpleName}")
                        val annotation = annotatedClass.getAnnotation(SqsConsumer::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, annotatedClass.simpleName)}:")
                        stringBuffer.appendln("    handler: ${annotatedClass.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - sqs:")
                        stringBuffer.appendln("          arn: ${annotation.sqsArn}")
                    }
                    stringBuffer
                }