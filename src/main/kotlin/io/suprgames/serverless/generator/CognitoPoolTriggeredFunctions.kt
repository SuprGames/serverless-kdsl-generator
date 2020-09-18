package io.suprgames.serverless.generator

import io.suprgames.serverless.CognitoUserPoolTriggered
import org.reflections.Reflections

class CognitoGenerator : Generator {
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
    override fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer =
            with(annotation as CognitoUserPoolTriggered) {
                StringBuffer()
                        .appendln("  ${name(name, defaultName)}:")
                        .appendln("    handler: $handlerName")
                        .appendln("    events:")
                        .appendln("      - cognitoUserPool:")
                        .appendln("          pool: $pool")
                        .appendln("          trigger: $trigger")
                        .appendln("          existing: $existing") as StringBuffer
            }
}

