package io.suprgames.serverless.generator

import io.suprgames.serverless.WebSocketConnector

class WebSocketConnectorGenerator : Generator {

    /**
     * This is how should look a WebSocketFunction entry
     *   websocket-disconnect:
     *     handler: io.suprgames.connector.handlers.DisconnectHandler
     *     events:
     *       - websocket:
     *           route: $disconnect
     */
    override fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer =
            with(annotation as WebSocketConnector) {
                StringBuffer()
                        .appendln("  ${name(name, defaultName)}:")
                        .appendln("    handler: $handlerName")
                        .appendln("    events:")
                        .appendln("      - websocket:")
                        .appendln("          route: ${annotation.route}")
            } as StringBuffer
}
