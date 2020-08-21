package io.suprgames.serverless.generator

import org.reflections.Reflections
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
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

        val reflections =
                Reflections(
                        ConfigurationBuilder()
                                .setUrls(ClasspathHelper.forPackage(basePackage))
                                .setScanners(SubTypesScanner(), TypeAnnotationsScanner()))

        println("    Setting package scanner")

        Files.write(
                Paths.get("serverless.yml"),
                Files.readAllLines(Paths.get(baseFile))
                        .toStringBuffer()
                        .appendln()
                        .appendln("functions:")
                        .append(websocketConnectors(reflections))
                        .append(sqsConsumers(reflections))
                        .append(httpFunctions(reflections))
                        .append(eventBridgeListeners(reflections))
                        .appendln()
                        .appendln(signature())
                        .toString()
                        .toByteArray(),
                StandardOpenOption.TRUNCATE_EXISTING)

        println("************************************************************")
        println("    File [serverless.yml] generated successfully")
        println("************************************************************")
    }
}