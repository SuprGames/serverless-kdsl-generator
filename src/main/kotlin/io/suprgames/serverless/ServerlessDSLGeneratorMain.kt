package io.suprgames.serverless

import io.suprgames.serverless.generator.ServerlessKDSLGenerator

object ServerlessDSLGeneratorMain {

    @JvmStatic
    fun main(vararg args: String) {
        println(args)
        if (args.size == 3) {
            ServerlessKDSLGenerator(args[0], args[1], args[2])
        } else {
            ServerlessKDSLGenerator()
        }.generate()
    }

}