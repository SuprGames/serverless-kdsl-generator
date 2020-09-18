package io.suprgames.serverless.generator

import io.suprgames.serverless.AuthorizerFunction
import org.reflections.Reflections


class AuthorizationFunctionGenerator : Generator {
    /**
     * This is how should look a HttpFunction entry
     *   playerAuthorizationCheck:
     *     handler: io.suprgames.operatorapi.player.AuthorizationCheck
     */
    override fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer =
            with(annotation as AuthorizerFunction) {
                StringBuffer()
                        .appendln("  ${name(name, defaultName)}:")
                        .appendln("    handler: $handlerName")
            } as StringBuffer
}