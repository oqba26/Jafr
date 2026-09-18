package com.oqba26.jafr

import android.util.Log
import com.oqba26.jafr.data.HistoryDao
import com.oqba26.jafr.data.HistoryEntity
import com.oqba26.jafr.model.HistoryItem
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HistoryDto(
    val id: Long? = null,
    val text: String,
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("mother_name") val motherName: String? = null,
    val result: Int,
    val answer: String? = null,
    val type: String,
    val timestamp: String,
    @SerialName("device_id") val deviceId: String? = null,
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
    @SerialName("device_id") val deviceId: String,
)

class HistoryManager(
    private val historyDao: HistoryDao,
    private val getDeviceId: () -> String,
) {
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
        // 1. Local-First: Collect local Room DB items continuously
        scope.launch {
            historyDao.getAllHistory().collectLatest { entities ->
                val devId = getDeviceId()
                val localItems = entities
                    .filter { (it.deviceId == null) || devId.isEmpty() || (it.deviceId == devId) }
                    .map { entity ->
                        HistoryItem(
                            id = entity.id,
                            text = entity.text,
                            firstName = entity.firstName,
                            motherName = entity.motherName,
                            result = entity.result,
                            answer = entity.answer,
                            type = try {
                                AbjadType.valueOf(entity.type)
                            } catch (_: Exception) {
                                AbjadType.JAFR_15
                            },
                            timestamp = entity.timestamp
                        )
                    }
                _historyList.value = localItems
            }
        }

        // 2. Online Sync: Sync with Supabase on startup
        scope.launch {
            refreshHistory()
        }
    }

    suspend fun refreshHistory() {
        withContext(Dispatchers.IO) {
            try {
                val devId = getDeviceId()
                if (devId.isEmpty()) return@withContext

                // Fetch remote items strictly for THIS deviceId
                val dtos = try {
                    supabase.from("jafr_history")
                        .select {
                            filter { eq("device_id", devId) }
                            order("id", Order.DESCENDING)
                        }
                        .decodeList<HistoryDto>()
                } catch (e: Exception) {
                    Log.e("HistoryManager", "Error fetching from Supabase", e)
                    emptyList()
                }

                if (dtos.isNotEmpty()) {
                    val existingEntities = historyDao.getAllHistoryList()
                    val dtosToInsert = dtos.filter { dto ->
                        existingEntities.none { local ->
                            local.timestamp == dto.timestamp && local.text == dto.text && local.type == dto.type
                        }
                    }

                    if (dtosToInsert.isNotEmpty()) {
                        val entitiesToInsert = dtosToInsert.map { dto ->
                            HistoryEntity(
                                text = dto.text,
                                firstName = dto.firstName,
                                motherName = dto.motherName,
                                result = dto.result,
                                answer = dto.answer,
                                type = dto.type,
                                timestamp = dto.timestamp,
                                deviceId = devId
                            )
                        }
                        historyDao.insertItems(entitiesToInsert)
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryManager", "Refresh history error", e)
            }
        }
    }

    suspend fun addHistoryItem(item: HistoryItem) {
        withContext(Dispatchers.IO) {
            try {
                val devId = getDeviceId()
                val current = _historyList.value
                val cleanItemText = AbjadUtils.stripYaHoo(item.text)

                // Check for exact duplicate locally
                val exactDuplicate = current.take(10).any {
                    AbjadUtils.stripYaHoo(it.text) == cleanItemText &&
                            it.answer == item.answer &&
                            it.type == item.type
                }
                if (exactDuplicate) return@withContext

                // Delete previous incomplete/partial edits of the same question locally & remotely
                val previousIncompleteMatch = current.take(15).firstOrNull { existing ->
                    existing.type == item.type &&
                            ((existing.firstName != null && item.firstName != null &&
                                    existing.firstName == item.firstName && existing.motherName == item.motherName) ||
                                    existing.text.trim() == cleanItemText) &&
                            (cleanItemText.startsWith(existing.text.trim().removeSuffix("؟").removeSuffix("?").trim()) ||
                                    existing.text.trim().startsWith(cleanItemText.removeSuffix("؟").removeSuffix("?").trim()))
                }

                previousIncompleteMatch?.let {
                    deleteHistoryItem(it.id)
                }

                // 1. SAVE TO ROOM DATABASE FIRST (Local-First)
                val newEntity = HistoryEntity(
                    text = cleanItemText,
                    firstName = item.firstName,
                    motherName = item.motherName,
                    result = item.result,
                    answer = item.answer,
                    type = item.type.name,
                    timestamp = item.timestamp,
                    deviceId = devId
                )
                historyDao.insertItem(newEntity)

                // 2. ONLINE SYNC TO SUPABASE
                try {
                    val dto = HistoryInsertDto(
                        text = cleanItemText,
                        firstName = item.firstName,
                        motherName = item.motherName,
                        result = item.result,
                        answer = item.answer,
                        type = item.type.name,
                        timestamp = item.timestamp,
                        deviceId = devId
                    )
                    supabase.from("jafr_history").insert(dto)
                } catch (e: Exception) {
                    Log.e("HistoryManager", "Error inserting to Supabase", e)
                }
            } catch (e: Exception) {
                Log.e("HistoryManager", "Error adding history item", e)
            }
        }
    }

    suspend fun deleteHistoryItem(id: Long) {
        withContext(Dispatchers.IO) {
            try {
                val item = _historyList.value.find { it.id == id }
                val devId = getDeviceId()

                // Delete locally from Room
                historyDao.deleteItem(id)

                // Delete remotely from Supabase strictly filtering by device_id
                if (item != null && devId.isNotEmpty()) {
                    try {
                        supabase.from("jafr_history").delete {
                            filter {
                                eq("device_id", devId)
                                eq("timestamp", item.timestamp)
                                eq("text", AbjadUtils.stripYaHoo(item.text))
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HistoryManager", "Error deleting from Supabase", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryManager", "Error deleting history item", e)
            }
        }
    }

    suspend fun clearHistory() {
        withContext(Dispatchers.IO) {
            try {
                val devId = getDeviceId()

                // 1. Clear local Room database
                historyDao.clearHistory()

                // 2. Delete ONLY this device's records from Supabase
                if (devId.isNotEmpty()) {
                    try {
                        supabase.from("jafr_history").delete {
                            filter { eq("device_id", devId) }
                        }
                    } catch (e: Exception) {
                        Log.e("HistoryManager", "Error clearing Supabase history", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("HistoryManager", "Error clearing history", e)
            }
        }
    }
}
