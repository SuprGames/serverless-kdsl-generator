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
        reflections.getTypesAnnotatedWith(HttpFunction::class.java)
                .let { classesWithAnnotation ->
                    val stringBuffer = StringBuffer()
                    println("      Generating [${classesWithAnnotation.size}] HTTP functions...")
                    classesWithAnnotation.forEach { annotatedClass ->
                        println("        ${annotatedClass.simpleName}")
                        val annotation = annotatedClass.getAnnotation(HttpFunction::class.java)
                        stringBuffer.appendln("  ${name(annotation.name, annotatedClass.simpleName)}:")
                        stringBuffer.appendln("    handler: ${annotatedClass.name}")
                        stringBuffer.appendln("    events:")
                        stringBuffer.appendln("      - http:")
                        stringBuffer.appendln("          path: ${annotation.path}")
                        stringBuffer.appendln("          method: ${annotation.method.toString().toLowerCase()}")
                        if (annotation.cors) {
                            stringBuffer.appendln("          cors: ${annotation.cors}")
                        }
                        if (annotation.authorizer.isNotBlank()) {
                            authorizerAnnotationByName(reflections, annotation.authorizer).let { aa ->
                                if (aa == null) {
                                    println("WARNING: Not able to find ExistingAuthorizerFunction or AuthorizerFunction mentioned in ${annotatedClass.simpleName}")
                                } else {
                                    stringBuffer.append(registerAuthorizer("name", aa.name, aa.ttl, aa.identitySources, aa.identityValidationExpression, aa.type))
                                }
                            }
                        }
                        if (annotatedClass.isAnnotationPresent(ExistingAuthorizerFunction::class.java)) {
                            val eaf = annotatedClass.getAnnotation(ExistingAuthorizerFunction::class.java)
                            stringBuffer.append(registerAuthorizer("arn", eaf.arn, eaf.ttl, eaf.identitySources, eaf.identityValidationExpression, eaf.type))
                            stringBuffer.appendln("            managedExternally: ${eaf.managedExternally}")
                        }
                    }
                    stringBuffer
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

