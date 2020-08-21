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
                .let {
                    val stringBuffer = StringBuffer()
                    println("      Generating [${it.size}] Sqs consumers...")
                    it.forEach {
                        println("        ${it.simpleName}")
                        val annotation = it.getAnnotation(SqsConsumer::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, it.simpleName)}:")
                        stringBuffer.appendln("    handler: ${it.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - sqs:")
                        stringBuffer.appendln("          arn: ${annotation.sqsArn}")
                    }
                    stringBuffer
                }