package com.oqba26.jafr.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["text", "timestamp"]),
        Index(value = ["deviceId"])
    ]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val firstName: String? = null,
    val motherName: String? = null,
    val result: Int,
    val answer: String? = null,
    val type: String,
    val timestamp: String,
    val deviceId: String? = null
)
