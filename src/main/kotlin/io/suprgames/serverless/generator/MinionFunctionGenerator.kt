package io.suprgames.serverless.generator

import io.suprgames.serverless.MinionFunction

class MinionFunctionGenerator : Generator {
    /**
     * This is how should look a MinionFunction entry
     *   registerEvent:
     *     handler: io.suprgames.triggered.UserRegistrationMinion
     */
    override fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer =
            with(annotation as MinionFunction) {
                StringBuffer()
                        .appendln("  ${name(name, defaultName)}:")
                        .appendln("    handler: $handlerName")
            } as StringBuffer
}
