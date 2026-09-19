package com.hackathon.finni.data.database


import androidx.room3.ColumnTypeConverter
import com.hackathon.finni.api.tag.model.TagId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import kotlin.uuid.Uuid

object DatabaseConverters {
    @ColumnTypeConverter
    fun fromStringList(value: List<String>?): String? {
        return value?.let { Json.encodeToString(it) }
    }

    @ColumnTypeConverter
    fun toStringList(value: String?): List<String>? {
        return value?.let { Json.decodeFromString<List<String>>(it) }
    }

    @ColumnTypeConverter
    fun fromListInt(list: List<Int>): String {
        return list.joinToString(",")
    }

    @ColumnTypeConverter
    fun toListInt(data: String): List<Int> {
        return listOf(*data.split(",").map { it.toInt() }.toTypedArray())
    }

    @ColumnTypeConverter
    fun fromUuid(uuid: Uuid): String = uuid.toString()

    @ColumnTypeConverter
    fun toUuid(data: String): Uuid = Uuid.parse(data)

    @ColumnTypeConverter
    fun fromInstant(instant: Instant): Long = instant.toEpochMilliseconds()

    @ColumnTypeConverter
    fun toInstant(data: Long): Instant = Instant.fromEpochMilliseconds(data)

    @ColumnTypeConverter
    fun fromTagIdList(value: List<TagId>?): String? {
        return value?.let { list -> Json.encodeToString(list.map { it.value.toString() }) }
    }

    @ColumnTypeConverter
    fun toTagIdList(value: String?): List<TagId>? {
        return value?.let { Json.decodeFromString<List<String>>(it).map { TagId(Uuid.parse(it)) } }
    }

    @ColumnTypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @ColumnTypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }
}