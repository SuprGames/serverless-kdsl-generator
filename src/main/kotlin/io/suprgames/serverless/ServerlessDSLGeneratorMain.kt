package io.suprgames.serverless

import io.suprgames.serverless.generator.ServerlessKDSLGenerator
import java.lang.RuntimeException

object ServerlessDSLGeneratorMain {

    @JvmStatic
    fun main(vararg args: String) {
        if (args.size == 3) {
            println("Arguments provided: " + args.asList())
            ServerlessKDSLGenerator(args[0], args[1], args[2])
        } else {
            println(" ************************************************************************************")
            println("   Syntax error, arguments number must be 3 and there are ${args.size}: ")
            println()
            println(" Provided: ${args.asList()}")
            println()
            println("   Three values are required: ")
            println("   - 1) baseFile:       Base file that will used to generate the serverless file from")
            println("   - 2) basePackage:    Base package that will be used to do the reflection")
            println("   - 3) serverlessFile: Serverless result file")
            println()
            println("   Argument list example: ")
            println("""    listOf("serverless-base.yml", "com.wizanit", "serverless.yml")""")
            println(" ************************************************************************************")
            println()
            println()
            throw RuntimeException("Invalid number of parameters provided ${args.size}, must be 3")
        }.generate()
    }

}