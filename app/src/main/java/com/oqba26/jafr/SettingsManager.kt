package com.oqba26.jafr

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val FONT_KEY = stringPreferencesKey("selected_font")
    }

    val selectedFont: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_KEY] ?: "vazirmatn_regular"
    }

    suspend fun saveFont(fontName: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_KEY] = fontName
        }
    }
}
