package com.example.treasurehuntapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "treasure_hunt_prefs")

@Singleton
class CurrentHuntStorage @Inject constructor(
    private val context: Context
) {
    private val KEY_HUNT_ID = stringPreferencesKey("current_hunt_id")
    private val KEY_HUNT_CODE = stringPreferencesKey("current_hunt_code")

    val huntIdFlow: Flow<String?> = context.dataStore.data.map { it[KEY_HUNT_ID] }
    val huntCodeFlow: Flow<String?> = context.dataStore.data.map { it[KEY_HUNT_CODE] }

    suspend fun saveHunt(id: String, code: String?) {
        context.dataStore.edit { prefs ->
            prefs[KEY_HUNT_ID] = id
            if (code != null) prefs[KEY_HUNT_CODE] = code
        }
    }

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_HUNT_ID)
            prefs.remove(KEY_HUNT_CODE)
        }
    }
}
