package com.catchyapps.whatsdelete.basicapputils

import java.net.MalformedURLException
import java.net.URL
import java.util.UUID

val String.Companion.empty: String get() = ""
val String.Companion.space: String get() = " "

fun String?.safeGet(): String = this ?: String.empty

fun String.nonAlphabetCharPresent(): Boolean = this.matches("^[a-zA-Z]*$".toRegex()).not()

fun String.removeWhiteSpaces(): String = this.replace("\\s".toRegex(), String.empty)

fun String.Companion.randomString(length: Int): String {
    val allowedChars = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz0123456789"
    return (1..length)
        .map { allowedChars.random() }
        .joinToString(String.empty)
}

fun String.removeEmojis(): String {
    val regex = Regex("[\\p{So}]")
    return this.replace(regex, String.empty)
}

fun String.Companion.generateSessionId(): String {
    return UUID.randomUUID().toString()
}

fun String.getBaseUrl(): String {
    return try {
        val url = URL(this)
        val baseUrl = url.host
        if (baseUrl.startsWith("www.")) {
            baseUrl.replace("www.", String.empty)
        }
        baseUrl
    } catch (e: MalformedURLException) {
        this
    }
}

fun String.isWebP() = endsWith(".webp", true)

fun String.isGif() = endsWith(".gif", true)

fun String.isApng() = endsWith(".apng", true)

fun String.isSvg() = endsWith(".svg", true)

fun String.getParentPath() = removeSuffix("/${getFilenameFromPath()}")

fun String.getFilenameFromPath() = substring(lastIndexOf("/") + 1)

fun String.areDigitsOnly() = matches(Regex("[0-9]+"))

fun String.capitalizeTheFolder(): String {
    val words = split(" ").toMutableList()
    val output = StringBuilder(String.empty)
    for (word in words) {
        output.append(word.replaceFirstChar { it.uppercase() } + " ")
    }
    return String(output).trim()
}

infix fun String.fromTable(tableName: String) = "${tableName}.${this}"