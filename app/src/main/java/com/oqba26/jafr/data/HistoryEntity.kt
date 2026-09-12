package com.oqba26.jafr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey val id: Long,
    val text: String,
    val firstName: String?,
    val motherName: String?,
    val result: Int,
    val answer: String?,
    val type: String,
    val timestamp: String
)
