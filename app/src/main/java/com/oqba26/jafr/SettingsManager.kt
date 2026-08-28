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
        val DEFAULT_TYPE_KEY = stringPreferencesKey("default_abjad_type")
    }

    val selectedFont: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_KEY] ?: "vazirmatn"
    }

    val defaultType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_TYPE_KEY] ?: "JAFR_15"
    }

    suspend fun saveFont(fontName: String) {
        context.dataStore.edit { preferences ->
            preferences[FONT_KEY] = fontName
        }
    }

    suspend fun saveDefaultType(typeName: String) {
        context.dataStore.edit { preferences ->
            preferences[DEFAULT_TYPE_KEY] = typeName
        }
    }
}
