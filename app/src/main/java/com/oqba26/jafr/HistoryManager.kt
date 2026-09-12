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
    val timestamp: String
)

class HistoryManager {
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
                val dtos = supabase.from("jafr_history")
                    .select {
                        order("id", Order.DESCENDING)
                    }
                    .decodeList<HistoryDto>()

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
                val isDuplicate = current.take(10).any {
                    it.text.trim() == item.text.trim() &&
                    it.answer == item.answer &&
                    it.type == item.type
                }

                if (!isDuplicate) {
                    val dto = HistoryDto(
                        id = item.id,
                        text = item.text,
                        firstName = item.firstName,
                        motherName = item.motherName,
                        result = item.result,
                        answer = item.answer,
                        type = item.type.name,
                        timestamp = item.timestamp
                    )
                    supabase.from("jafr_history").insert(dto)
                    refreshHistory()
                }
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
