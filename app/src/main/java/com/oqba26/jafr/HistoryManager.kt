package com.oqba26.jafr

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import com.oqba26.jafr.model.HistoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Serializable
data class HistoryDto(
    val id: Long,
    val text: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("mother_name") val motherName: String? = null,
    val result: Int,
    val answer: String? = null,
    val type: String,
    val timestamp: String,
    @SerialName("device_id") val deviceId: String? = null
)

@Serializable
data class HistoryInsertDto(
    val text: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("mother_name") val motherName: String? = null,
    val result: Int,
    val answer: String? = null,
    val type: String,
    val timestamp: String,
    @SerialName("device_id") val deviceId: String? = null
)

@Serializable
data class HistoryInsertFallbackDto(
    val text: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("mother_name") val motherName: String? = null,
    val result: Int,
    val answer: String? = null,
    val type: String,
    val timestamp: String
)

class HistoryManager(private val getDeviceId: (suspend () -> String)? = null) {
    private val supabase = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
    ) {
        install(Postgrest)
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _historyList = MutableStateFlow<List<HistoryItem>>(emptyList())
    val historyList: StateFlow<List<HistoryItem>> = _historyList.asStateFlow()

    init {
        scope.launch {
            refreshHistory()
        }
    }

    suspend fun refreshHistory() {
        withContext(Dispatchers.IO) {
            try {
                val devId = getDeviceId?.invoke() ?: ""
                val dtos = if (devId.isNotEmpty()) {
                    try {
                        val filtered = supabase.from("jafr_history")
                            .select {
                                filter { eq("device_id", devId) }
                                order("id", Order.DESCENDING)
                            }
                            .decodeList<HistoryDto>()

                        filtered.ifEmpty {
                            supabase.from("jafr_history")
                                .select {
                                    order("id", Order.DESCENDING)
                                }
                                .decodeList<HistoryDto>()
                        }
                    } catch (_: Exception) {
                        supabase.from("jafr_history")
                            .select {
                                order("id", Order.DESCENDING)
                            }
                            .decodeList<HistoryDto>()
                    }
                } else {
                    supabase.from("jafr_history")
                        .select {
                            order("id", Order.DESCENDING)
                        }
                        .decodeList<HistoryDto>()
                }

                val items = dtos.map { dto ->
                    HistoryItem(
                        id = dto.id,
                        text = dto.text,
                        firstName = dto.firstName,
                        motherName = dto.motherName,
                        result = dto.result,
                        answer = dto.answer,
                        type = try { AbjadType.valueOf(dto.type) } catch (_: Exception) { AbjadType.JAFR_15 },
                        timestamp = dto.timestamp
                    )
                }
                _historyList.value = items
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun addHistoryItem(item: HistoryItem) {
        withContext(Dispatchers.IO) {
            try {
                val current = _historyList.value
                val cleanItemText = AbjadUtils.stripYaHoo(item.text)

                // Check for exact duplicate
                val exactDuplicate = current.take(10).any {
                    AbjadUtils.stripYaHoo(it.text) == cleanItemText &&
                    it.answer == item.answer &&
                    it.type == item.type
                }

                if (exactDuplicate) return@withContext

                // Delete previous incomplete/partial edits of the same question
                val previousIncompleteMatch = current.take(15).firstOrNull { existing ->
                    existing.type == item.type &&
                    ((existing.firstName != null && item.firstName != null &&
                      existing.firstName == item.firstName && existing.motherName == item.motherName) ||
                     existing.text.trim() == cleanItemText) &&
                    (cleanItemText.startsWith(existing.text.trim().removeSuffix("؟").removeSuffix("?").trim()) ||
                     existing.text.trim().startsWith(cleanItemText.removeSuffix("؟").removeSuffix("?").trim()))
                }

                if (previousIncompleteMatch != null) {
                    try {
                        supabase.from("jafr_history").delete {
                            filter { eq("id", previousIncompleteMatch.id) }
                        }
                    } catch (_: Exception) {}
                }

                val devId = getDeviceId?.invoke() ?: ""
                try {
                    val dto = HistoryInsertDto(
                        text = cleanItemText,
                        firstName = item.firstName,
                        motherName = item.motherName,
                        result = item.result,
                        answer = item.answer,
                        type = item.type.name,
                        timestamp = item.timestamp,
                        deviceId = devId.ifEmpty { null }
                    )
                    supabase.from("jafr_history").insert(dto)
                } catch (_: Exception) {
                    val fallbackDto = HistoryInsertFallbackDto(
                        text = cleanItemText,
                        firstName = item.firstName,
                        motherName = item.motherName,
                        result = item.result,
                        answer = item.answer,
                        type = item.type.name,
                        timestamp = item.timestamp
                    )
                    supabase.from("jafr_history").insert(fallbackDto)
                }
                refreshHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun deleteHistoryItem(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                supabase.from("jafr_history").delete {
                    filter {
                        eq("id", id)
                    }
                }
                refreshHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            try {
                val allItems = _historyList.value
                allItems.forEach { item ->
                    supabase.from("jafr_history").delete {
                        filter { eq("id", item.id) }
                    }
                }
                refreshHistory()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
