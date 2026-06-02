package com.example.treasurehuntapp.data.source.remote.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedTokenStorage @Inject constructor(
    private val context: Context,
    private val appScope: CoroutineScope
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secure_auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val KEY_ACCESS = "access_token"
    private val KEY_REFRESH = "refresh_token"
    private val KEY_USER_ID = "user_id"
    private val KEY_USERNAME = "username"
    private val KEY_EMAIL = "email"

    private val _refreshToken = MutableStateFlow<String?>(null)
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()

    private val _userId = MutableStateFlow<String?>(null)
    val userId: StateFlow<String?> = _userId.asStateFlow()

    private val _accessToken = MutableStateFlow<String?>(null)
    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()

    private val preferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            KEY_ACCESS -> _accessToken.value = sharedPreferences.getString(KEY_ACCESS, null)
            KEY_REFRESH -> _refreshToken.value = sharedPreferences.getString(KEY_REFRESH, null)
            KEY_USER_ID -> _userId.value = sharedPreferences.getString(KEY_USER_ID, null)
        }
    }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
        appScope.launch {
            loadTokens()
        }
    }

    private fun loadTokens() {
        _accessToken.value = sharedPreferences.getString(KEY_ACCESS, null)
        _refreshToken.value = sharedPreferences.getString(KEY_REFRESH, null)
        _userId.value = sharedPreferences.getString(KEY_USER_ID, null)
    }

    fun forceLoadNow() {
        loadTokens()
    }

    fun accessTokenNow(): String? = _accessToken.value
    fun refreshTokenNow(): String? = _refreshToken.value
    fun userIdNow(): String? = _userId.value

    fun save(
        userId: String,
        username: String,
        email: String,
        accessToken: String,
        refreshToken: String
    ) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_USERNAME, username)
            putString(KEY_EMAIL, email)
            putString(KEY_ACCESS, accessToken)
            putString(KEY_REFRESH, refreshToken)
            apply()
        }
        _accessToken.value = accessToken
        _refreshToken.value = refreshToken
        _userId.value = userId
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
        _accessToken.value = null
        _refreshToken.value = null
        _userId.value = null
    }
}