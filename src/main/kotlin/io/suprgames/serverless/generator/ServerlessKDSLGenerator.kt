package io.suprgames.serverless.generator

import io.suprgames.serverless.*
import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

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
                Paths.get(serverlessFile),
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
                        .append(generate(reflections, MinionFunction::class.java, MinionFunctionGenerator()))
                        .appendln()
                        .appendln(signature())
                        .toString()
                        .toByteArray(),
                StandardOpenOption.TRUNCATE_EXISTING)

        println("************************************************************")
        println("    File [$serverlessFile] generated successfully")
        println("************************************************************")

        Files.write(
                Paths.get("js-serverless/$serverlessFile"),
                Files.readAllLines(Paths.get("js-serverless/$baseFile"))
                        .toStringBuffer()
                        .appendln()
                        .appendln("functions:")
                        //Http functions are a bit different
                        .append(httpFunctions(reflections, true))
                        .append(generate(reflections, WebSocketConnector::class.java, WebSocketConnectorGenerator(), true))
                        .append(generate(reflections, SqsConsumer::class.java, SqsConsumerGenerator(), true))
                        .append(generate(reflections, EventBridgeListener::class.java, EventBridgeListenerGenerator(), true))
                        .append(generate(reflections, AuthorizerFunction::class.java, AuthorizationFunctionGenerator(), true))
                        .append(generate(reflections, CognitoUserPoolTriggered::class.java, CognitoGenerator(), true))
                        .append(generate(reflections, MinionFunction::class.java, MinionFunctionGenerator(), true))
                        .appendln()
                        .appendln(signature())
                        .toString()
                        .toByteArray(),
                StandardOpenOption.TRUNCATE_EXISTING)

        println("*********************************************************************")
        println("    File [js-serverless/$serverlessFile] generated successfully")
        println("*********************************************************************")
    }

    fun <T : Annotation> generate(reflections: Reflections, clazz: Class<T>, generator: Generator, alterEgoPhase: Boolean = false): StringBuffer = forClasses(reflections, clazz, generator, alterEgoPhase).append(forMethods(reflections, clazz, generator, alterEgoPhase))

    private fun <T : Annotation> forClasses(reflections: Reflections, clazz: Class<T>, generator: Generator, alterEgoPhase: Boolean): StringBuffer =
            reflections.getTypesAnnotatedWith(clazz)
                    .let { classesWithAnnotation ->
                        val stringBuffer = StringBuffer()
                        var processed = 0
                        classesWithAnnotation.forEach { annotatedClass ->
                            val annotation = annotatedClass.getAnnotation(clazz)
                            val defaultName = annotatedClass.simpleName
                            val handlerName = generateHandlerNameForClass(annotatedClass, alterEgoPhase)
                            if (handlerName.isNotBlank()) {
                                processed ++
                                stringBuffer.append(generator.generate(defaultName, handlerName, annotation))
                                stringBuffer.append(addRuntimeIfNeeded(annotatedClass, alterEgoPhase))
                            }
                        }
                        println("      Generating [$processed] ${clazz.simpleName} for Class...")
                        stringBuffer
                    }

    /**
     * If we have an alter ego for the method/class we will specify its Runtime
     */
    private fun addRuntimeIfNeeded(element: AnnotatedElement, alterEgoPhase: Boolean): StringBuffer {
        val sb = StringBuffer()
        if (!alterEgoPhase) {
            val runtime: String? = (element.annotations.firstOrNull { it is AlterEgo } as? AlterEgo)?.runtime
            if (runtime != null && runtime != AlterEgoRuntimes.NODEJS_12) {
                sb.appendln("    runtime: $runtime")
            }
        }
        return sb
    }

    private fun generateHandlerNameForClass(annotatedClass: Class<*>, alterEgoPhase: Boolean): String {
        val alterEgo = (annotatedClass.annotations.firstOrNull { it is AlterEgo } as? AlterEgo)
        return when {
            alterEgoPhase && alterEgo != null && alterEgo.handlerPath.isNotBlank() -> alterEgo.handlerPath
            alterEgoPhase && alterEgo != null -> "src/" + annotatedClass.name.split(".").joinToString("/") + ".handler"
            !alterEgoPhase && alterEgo == null -> annotatedClass.name
            else -> ""
        }
    }

    private fun <T : Annotation> forMethods(reflections: Reflections, clazz: Class<T>, generator: Generator, alterEgoPhase: Boolean): StringBuffer =
            reflections.getMethodsAnnotatedWith(clazz)
                    .let { methodsWithAnnotation ->
                        val stringBuffer = StringBuffer()
                        var processed = 0
                        methodsWithAnnotation.forEach { annotatedMethod ->
                            val annotation = annotatedMethod.getAnnotation(clazz)
                            val defaultName = annotatedMethod.declaringClass.simpleName + "-" + annotatedMethod.name
                            val handlerName = generateHandlerNameForMethod(annotatedMethod, alterEgoPhase)
                            if (handlerName.isNotBlank()) {
                                processed++
                                stringBuffer.append(generator.generate(defaultName, handlerName, annotation))
                                stringBuffer.append(addRuntimeIfNeeded(annotatedMethod, alterEgoPhase))
                            }
                        }
                        println("      Generating [${methodsWithAnnotation.size}] ${clazz.simpleName} for Methods...")
                        stringBuffer
                    }

    private fun generateHandlerNameForMethod(annotatedMethod: Method, alterEgoPhase: Boolean): String {
        val alterEgo = (annotatedMethod.annotations.firstOrNull { it is AlterEgo } as? AlterEgo)
        return when {
            alterEgoPhase && alterEgo != null && alterEgo.handlerPath.isNotBlank() -> alterEgo.handlerPath
            alterEgoPhase && alterEgo != null -> "src/" + annotatedMethod.declaringClass.name.split(".").joinToString("/") + "." + annotatedMethod.name
            !alterEgoPhase && alterEgo == null -> "${annotatedMethod.declaringClass.name}::${annotatedMethod.name}"
            else -> ""
        }
    }


}