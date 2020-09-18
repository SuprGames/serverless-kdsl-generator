package io.suprgames.serverless.generator

interface Generator {
    fun generate(defaultName: String, handlerName: String, annotation: Annotation): StringBuffer
}