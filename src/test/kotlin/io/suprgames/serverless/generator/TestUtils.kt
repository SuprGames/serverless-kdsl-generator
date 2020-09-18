package io.suprgames.serverless.generator

import org.reflections.Reflections
import org.reflections.scanners.MethodAnnotationsScanner
import org.reflections.scanners.SubTypesScanner
import org.reflections.scanners.TypeAnnotationsScanner
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder

val reflections =
        Reflections(
                ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage("io.suprgames"))
                        .setScanners(SubTypesScanner(), TypeAnnotationsScanner(), MethodAnnotationsScanner()))