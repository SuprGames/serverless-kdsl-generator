package io.suprgames.serverless.generator

import io.suprgames.serverless.EventBridgeListener

class EventBridgeListenerGenerator : Generator {

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
    override fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer =
            with(annotation as EventBridgeListener) {
                StringBuffer()
                        .appendln("  ${name(name, defaultName)}:")
                        .appendln("    handler: $handlerName")
                        .appendln("    events:")
                        .appendln("      - eventBridge:")
                        .appendln("          eventBus: ${annotation.eventBusArn}")
                        .appendln("          pattern:")
                        .appendln("            detail-type:")
                        .appendln("              - '${annotation.eventToListen.qualifiedName}'")
            } as StringBuffer
}
