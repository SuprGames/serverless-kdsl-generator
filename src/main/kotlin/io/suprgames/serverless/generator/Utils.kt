package io.suprgames.serverless.generator

import java.util.*

fun name(givenName: String, className: String) =
        if (givenName.isNotBlank())
            givenName
        else
            className.split(Regex("(?=\\p{Lu})")).drop(1).joinToString("-").toLowerCase()

fun signature() = "#File generated ${Date()} with SuprGames.IO Serverless KotlinDSL"

fun Iterable<CharSequence>.toStringBuffer(): StringBuffer {
    val sb = StringBuffer()
    this.forEach { sb.appendln(it) }
    return sb
}