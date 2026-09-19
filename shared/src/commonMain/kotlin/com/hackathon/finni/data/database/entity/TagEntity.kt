package com.hackathon.finni.data.database.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.hackathon.finni.api.tag.model.TagId

@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    @ColumnInfo(name = "tag_id")
    val tagId: TagId,
    val name: String,
    val color: String
)
