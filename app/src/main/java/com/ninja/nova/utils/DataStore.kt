package com.ninja.nova.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nova_prefs")

object PrefsManager {
    private val TOKEN_KEY = stringPreferencesKey(Constants.PREF_TOKEN)
    private val SESSION_KEY = stringPreferencesKey(Constants.PREF_SESSION)

    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }

    suspend fun getToken(context: Context): String? {
        return context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun clearToken(context: Context) {
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }

    suspend fun saveSession(context: Context, sessionId: String) {
        context.dataStore.edit { it[SESSION_KEY] = sessionId }
    }

    suspend fun getSession(context: Context): String? {
        return context.dataStore.data.map { it[SESSION_KEY] }.first()
    }
}
