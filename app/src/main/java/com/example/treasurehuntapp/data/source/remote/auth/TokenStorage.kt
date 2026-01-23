package com.example.treasurehuntapp.data.remote.auth

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("auth_prefs")

class TokenStorage(private val context: Context) {

    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_EMAIL = stringPreferencesKey("email")

    suspend fun save(
        userId: String,
        username: String,
        email: String,
        accessToken: String,
        refreshToken: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = userId
            prefs[KEY_USERNAME] = username
            prefs[KEY_EMAIL] = email
            prefs[KEY_ACCESS] = accessToken
            prefs[KEY_REFRESH] = refreshToken
        }
    }

    suspend fun accessToken(): String? =
        context.dataStore.data.first()[KEY_ACCESS]

    suspend fun refreshToken(): String? =
        context.dataStore.data.first()[KEY_REFRESH]

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
