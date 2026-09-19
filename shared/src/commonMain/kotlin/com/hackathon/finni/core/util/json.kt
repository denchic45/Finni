package com.hackathon.finni.core.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonBuilder

val appJson = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    prettyPrint = true
}

val prettyJson = Json {
    baseJsonConfig()
    prettyPrint = true // Для логов или экспорта в файлы
}

inline fun <reified T> String.fromJson(): T = appJson.decodeFromString(this)

inline fun <reified T> T.toJson(): String = appJson.encodeToString(this)

inline fun <reified T> String.fromJsonOrNull(): T? = runCatching {
    appJson.decodeFromString<T>(this)
}.getOrNull()

private val baseJsonConfig: JsonBuilder.() -> Unit = {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
    // Здесь можно добавить общие модули, например для работы с датами
}
