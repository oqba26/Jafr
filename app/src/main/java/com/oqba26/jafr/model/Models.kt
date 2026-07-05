package com.oqba26.jafr.model

import com.oqba26.jafr.AbjadType

data class HistoryItem(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val result: Int,
    val type: AbjadType,
    val timestamp: String
)

enum class Screen { CALCULATOR, SETTINGS, HISTORY }
