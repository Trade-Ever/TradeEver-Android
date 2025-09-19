package com.trever.android.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.preferencesDataStore

private const val DS_NAME = "auth_tokens"
private val Context.dataStore by preferencesDataStore(name = DS_NAME)

class TokenStore(private val context: Context) {

    companion object {
        private val KEY_ACCESS = stringPreferencesKey("access_token")
        private val KEY_REFRESH = stringPreferencesKey("refresh_token")
    }

    val accessTokenFlow = context.dataStore.data.map { it[KEY_ACCESS] }
    val refreshTokenFlow = context.dataStore.data.map { it[KEY_REFRESH] }

    suspend fun saveTokens(access: String, refresh: String) {
        context.dataStore.edit {
            it[KEY_ACCESS] = access
            it[KEY_REFRESH] = refresh
        }
    }

    suspend fun getAccessToken(): String? = accessTokenFlow.first()
    suspend fun getRefreshToken(): String? = refreshTokenFlow.first()

    suspend fun clear() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_ACCESS)
            prefs.remove(KEY_REFRESH)
        }
    }
}