package com.oqba26.jafr.model

import com.oqba26.jafr.AbjadType

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val firstName: String? = null,
    val motherName: String? = null,
    val result: Int,
    val answer: String? = null,
    val type: AbjadType,
    val timestamp: String
)

enum class Screen { CALCULATOR, SETTINGS, HISTORY }
