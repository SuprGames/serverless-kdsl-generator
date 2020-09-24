package io.suprgames.serverless.generator

import io.suprgames.serverless.SqsConsumer

class SqsConsumerGenerator : Generator {
    /**
     * This is how should look a SqsConsumerFunction entry
     *   registerEvent:
     *     handler: src/Registrar.handler
     *     events:
     *       - sqs:
     *           arn: arn:aws:sqs:${self:provider.region}:#{AWS::AccountId}:${self:custom.eventQueueName}
     */
    override fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer =
            with(annotation as SqsConsumer) {
                StringBuffer()
                        .appendln("  ${name(name, defaultName)}:")
                        .appendln("    handler: $handlerName")
                        .appendln("    events:")
                        .appendln("      - sqs:")
                        .appendln("          arn: ${annotation.sqsArn}")
            } as StringBuffer
}
