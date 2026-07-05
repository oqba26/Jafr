package com.oqba26.jafr

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.oqba26.jafr.model.HistoryItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.historyDataStore by preferencesDataStore(name = "history")

class HistoryManager(private val context: Context) {
    private val gson = Gson()
    private val historyKey = stringPreferencesKey("history_list")

    val historyList: Flow<List<HistoryItem>> = context.historyDataStore.data.map { preferences ->
        val json = preferences[historyKey] ?: "[]"
        val type = object : TypeToken<List<HistoryItem>>() {}.type
        gson.fromJson(json, type)
    }

    suspend fun addHistoryItem(item: HistoryItem) {
        context.historyDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            val currentList: MutableList<HistoryItem> = gson.fromJson(currentJson, type)
            
            currentList.add(0, item)
            preferences[historyKey] = gson.toJson(currentList)
        }
    }

    suspend fun deleteHistoryItem(id: Long) {
        context.historyDataStore.edit { preferences ->
            val currentJson = preferences[historyKey] ?: "[]"
            val type = object : TypeToken<List<HistoryItem>>() {}.type
            val currentList: MutableList<HistoryItem> = gson.fromJson(currentJson, type)
            
            currentList.removeAll { it.id == id }
            preferences[historyKey] = gson.toJson(currentList)
        }
    }

    suspend fun clearHistory() {
        context.historyDataStore.edit { preferences ->
            preferences[historyKey] = "[]"
        }
    }
}
