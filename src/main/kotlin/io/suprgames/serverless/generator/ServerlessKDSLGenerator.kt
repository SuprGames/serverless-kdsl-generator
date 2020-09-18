package io.suprgames.serverless.generator

import io.suprgames.serverless.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.reflect.KFunction4

class ServerlessKDSLGenerator(
        val baseFile: String = "serverless-base.yml",
        val basePackage: String = "io.suprgames",
        val serverlessFile: String = "serverless.yml") {

    fun generate() {
        println("************************************************************")
        println("  Generating Serverless Framework file:")
        println("    Base file       : $baseFile")
        println("    Base package    : $basePackage")
        println("    Serverless file : $serverlessFile")
        println("************************************************************")

        println("    Setting package scanner")

        val reflections =
                Reflections(
                        ConfigurationBuilder()
                                .setUrls(ClasspathHelper.forPackage(basePackage))
                                .setScanners(SubTypesScanner(), TypeAnnotationsScanner(), MethodAnnotationsScanner()))

        Files.write(
                Paths.get("serverless.yml"),
                Files.readAllLines(Paths.get(baseFile))
                        .toStringBuffer()
                        .appendln()
                        .appendln("functions:")
                        //Http functions are a bit different
                        .append(httpFunctions(reflections))
                        .append(generate(reflections, WebSocketConnector::class.java, WebSocketConnectorGenerator()))
                        .append(generate(reflections, SqsConsumer::class.java, SqsConsumerGenerator()))
                        .append(generate(reflections, EventBridgeListener::class.java, EventBridgeListenerGenerator()))
                        .append(generate(reflections, AuthorizerFunction::class.java, AuthorizationFunctionGenerator()))
                        .append(generate(reflections, CognitoUserPoolTriggered::class.java, CognitoGenerator()))
                        .appendln()
                        .appendln(signature())
                        .toString()
                        .toByteArray(),
                StandardOpenOption.TRUNCATE_EXISTING)

        println("************************************************************")
        println("    File [serverless.yml] generated successfully")
        println("************************************************************")
    }

    fun <T : Annotation> generate(reflections: Reflections, clazz: Class<T>, generator: Generator): StringBuffer = forClasses(reflections, clazz, generator).append(forMethods(reflections, clazz, generator))

    private fun <T : Annotation> forClasses(reflections: Reflections, clazz: Class<T>, generator: Generator): StringBuffer =
            reflections.getTypesAnnotatedWith(clazz)
                    .let { classesWithAnnotation ->
                        val stringBuffer = StringBuffer()
                        println("      Generating [${classesWithAnnotation.size}] ${clazz.simpleName} for Class...")
                        classesWithAnnotation.forEach { annotatedClass ->
                            val annotation = annotatedClass.getAnnotation(clazz)
                            val defaultName = annotatedClass.simpleName
                            val handlerName = annotatedClass.name
                            stringBuffer.append(generator.generate(defaultName, handlerName, annotation))
                        }
                        stringBuffer
                    }

    private fun <T : Annotation> forMethods(reflections: Reflections, clazz: Class<T>, generator: Generator): StringBuffer =
            reflections.getMethodsAnnotatedWith(clazz)
                    .let { methodsWithAnnotation ->
                        val stringBuffer = StringBuffer()
                        println("      Generating [${methodsWithAnnotation.size}] ${clazz.simpleName} for Methods...")
                        methodsWithAnnotation.forEach { annotatedMethod ->
                            val annotation = annotatedMethod.getAnnotation(clazz)
                            val defaultName = annotatedMethod.declaringClass.simpleName + "-" + annotatedMethod.name
                            val handlerName = "${annotatedMethod.declaringClass.name}::${annotatedMethod.name}"
                            stringBuffer.append(generator.generate(defaultName, handlerName, annotation))
                        }
                        stringBuffer
                    }
}