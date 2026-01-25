package com.example.treasurehuntapp.data.source.remote.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("auth_prefs")

class TokenStorage(
    private val context: Context,
    appScope: CoroutineScope
) {
    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    private val KEY_USER_ID = stringPreferencesKey("user_id")
    private val KEY_USERNAME = stringPreferencesKey("username")
    private val KEY_EMAIL = stringPreferencesKey("email")

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    init {
        appScope.launch {
            context.dataStore.data
                .map { prefs -> prefs[KEY_ACCESS] }
                .distinctUntilChanged()
                .collect { token -> _accessToken.value = token }
        }
        appScope.launch {
            context.dataStore.data
                .map { prefs -> prefs[KEY_REFRESH] }
                .distinctUntilChanged()
                .collect { token -> _refreshToken.value = token }
        }
        appScope.launch {
            context.dataStore.data
                .map { prefs -> prefs[KEY_USER_ID] }
                .distinctUntilChanged()
                .collect { id -> _userId.value = id }
        }
    }
    
    suspend fun forceLoadNow() {
        val prefs = context.dataStore.data.first()
        _accessToken.value = prefs[KEY_ACCESS]
        _refreshToken.value = prefs[KEY_REFRESH]
        _userId.value = prefs[KEY_USER_ID]
    }
    
    fun accessTokenNow(): String? = _accessToken.value
    fun refreshTokenNow(): String? = _refreshToken.value
    fun userIdNow(): String? = _userId.value

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
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _userId.value = userId
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
        _accessToken.value = null
        _refreshToken.value = null
        _userId.value = null
    }
}
