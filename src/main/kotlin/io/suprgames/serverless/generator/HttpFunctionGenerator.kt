package io.suprgames.serverless.generator

import io.suprgames.serverless.AuthorizerFunction
import io.suprgames.serverless.AuthorizerFunctionType
import io.suprgames.serverless.ExistingAuthorizerFunction
import io.suprgames.serverless.HttpFunction
import org.reflections.Reflections

/**
 * This is how should look a HttpFunction entry
 *   registerPlayer:
 *     handler: io.suprgames.operatorapi.player.RegisterPlayerHandler
 *     events:
 *       - http:
 *           path: player/register
 *           method: post
 *           cors: true
 *           authorizer: myAuthorizerFunction
 */
fun httpFunctions(reflections: Reflections): StringBuffer =
        httpFunctionsForClasses(reflections).append(httpFunctionsForMethods(reflections))

private fun httpFunctionsForClasses(reflections: Reflections): StringBuffer {
    return reflections.getTypesAnnotatedWith(HttpFunction::class.java)
            .let { classesWithAnnotation ->
                val stringBuffer = StringBuffer()
                println("      Generating [${classesWithAnnotation.size}] HTTP Class functions...")
                classesWithAnnotation.forEach { annotatedClass ->
                    val annotation = annotatedClass.getAnnotation(HttpFunction::class.java)
                    val handlerName = annotatedClass.name
                    val entryName = name(annotation.name, annotatedClass.simpleName)
                    val eaf = annotatedClass.getAnnotation(ExistingAuthorizerFunction::class.java)
                    stringBuffer.append(generateHttpEntry(entryName, handlerName, annotation, reflections, eaf))
                }
                stringBuffer
            }
}

private fun httpFunctionsForMethods(reflections: Reflections): StringBuffer {
    return reflections.getMethodsAnnotatedWith(HttpFunction::class.java)
            .let { methodsWithAnnotation ->
                val stringBuffer = StringBuffer()
                println("      Generating [${methodsWithAnnotation.size}] HTTP Method functions...")
                methodsWithAnnotation.forEach { annotatedMethod ->
                    val annotation = annotatedMethod.getAnnotation(HttpFunction::class.java)
                    val handlerName = "${annotatedMethod.declaringClass.name}::${annotatedMethod.name}"
                    val entryName = name(annotation.name, annotatedMethod.declaringClass.simpleName + "-" + annotatedMethod.name)
                    val eaf = annotatedMethod.getAnnotation(ExistingAuthorizerFunction::class.java)
                    stringBuffer.append(generateHttpEntry(entryName, handlerName, annotation, reflections, eaf))
                }
                stringBuffer
            }
}

private fun generateHttpEntry(entryName: String, handlerName: String, annotation: HttpFunction, reflections: Reflections, eaf: ExistingAuthorizerFunction?): StringBuffer {
    val sb = StringBuffer()
    sb.appendln("  $entryName:")
    sb.appendln("    handler: $handlerName")
    sb.appendln("    events:")
    sb.appendln("      - http:")
    sb.appendln("          path: ${annotation.path}")
    sb.appendln("          method: ${annotation.method.toString().toLowerCase()}")
    if (annotation.cors) {
        sb.appendln("          cors: ${annotation.cors}")
    }
    if (annotation.authorizer.isNotBlank()) {
        authorizerAnnotationByName(reflections, annotation.authorizer).let { aa ->
            if (aa == null) {
                println("WARNING: Not able to find ExistingAuthorizerFunction or AuthorizerFunction mentioned in $handlerName")
            } else {
                sb.append(registerAuthorizer("name", aa.name, aa.ttl, aa.identitySources, aa.identityValidationExpression, aa.type))
            }
        }
    }
    if (eaf != null) {
        sb.append(registerAuthorizer("arn", eaf.arn, eaf.ttl, eaf.identitySources, eaf.identityValidationExpression, eaf.type))
        sb.appendln("            managedExternally: ${eaf.managedExternally}")
    }
    return sb
}

private fun authorizerAnnotationByName(reflections: Reflections, authorizerName: String): AuthorizerFunction? =
        reflections.getTypesAnnotatedWith(AuthorizerFunction::class.java).map { it.getAnnotation(AuthorizerFunction::class.java) }.find { it.name == authorizerName }

private fun registerAuthorizer(idType: String, id: String, ttl: Long, identitySources: Array<String>, idValExp: String, type: AuthorizerFunctionType): StringBuffer {
    val stringBuffer = StringBuffer()
    stringBuffer.appendln("          authorizer:")
    //We need to process the present Existing Authorizer Function annotation
    stringBuffer.appendln("            $idType: $id")
    stringBuffer.appendln("            resultTtlInSeconds: $ttl")
    if (identitySources.isNotEmpty()) {
        stringBuffer.appendln("            identitySource: ${identitySources.joinToString(", ")}")
    }
    if (idValExp.isNotBlank()) {
        stringBuffer.appendln("            identityValidationExpression: $idValExp")
    }
    stringBuffer.appendln("            type: ${type.toString().toLowerCase()}")
    return stringBuffer
}

