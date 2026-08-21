package com.bsnutrition.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthTokenDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : AuthTokenDataSource {

    private object PreferencesKeys {
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
        val USER_ID = longPreferencesKey("user_id")
    }

    override val authToken: Flow<String?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.AUTH_TOKEN]
    }

    override val userId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_ID]
    }

    override suspend fun saveAuth(token: String, userId: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTH_TOKEN] = token
            preferences[PreferencesKeys.USER_ID] = userId
        }
    }

    override suspend fun clearAuth() {
        dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.AUTH_TOKEN)
            preferences.remove(PreferencesKeys.USER_ID)
        }
    }
}
