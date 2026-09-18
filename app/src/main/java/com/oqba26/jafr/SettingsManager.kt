package com.oqba26.jafr

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {
    companion object {
        val FONT_KEY = stringPreferencesKey("selected_font")
        val DEFAULT_TYPE_KEY = stringPreferencesKey("default_abjad_type")
        val SHOW_KABIR_KEY = booleanPreferencesKey("show_kabir")
        val SHOW_SAGHIR_KEY = booleanPreferencesKey("show_saghir")
        val SHOW_WASAIT_KEY = booleanPreferencesKey("show_wasait")
        val DEVICE_ID_KEY = stringPreferencesKey("device_id")
    }

    val selectedFont: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[FONT_KEY] ?: "vazirmatn"
    }

    val defaultType: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[DEFAULT_TYPE_KEY] ?: "JAFR_15"
    }

    val showKabir: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_KABIR_KEY] ?: false
    }

    val showSaghir: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_SAGHIR_KEY] ?: false
    }

    val showWasait: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SHOW_WASAIT_KEY] ?: false
    }


    suspend fun getOrCreateDeviceId(): String {
        var id = ""
        context.dataStore.edit { preferences ->
            val current = preferences[DEVICE_ID_KEY]
            if (current.isNullOrEmpty()) {
                id = UUID.randomUUID().toString()
                preferences[DEVICE_ID_KEY] = id
            } else {
                id = current
            }
        }
        return id
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

    suspend fun saveShowKabir(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_KABIR_KEY] = show
        }
    }

    suspend fun saveShowSaghir(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_SAGHIR_KEY] = show
        }
    }

    suspend fun saveShowWasait(show: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SHOW_WASAIT_KEY] = show
        }
    }
}
