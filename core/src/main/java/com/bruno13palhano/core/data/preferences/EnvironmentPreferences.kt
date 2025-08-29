package com.bruno13palhano.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "environment_preferences")

@Singleton
internal class EnvironmentPreferences @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_ENV_ID = longPreferencesKey("last_environment_id")
    }

    val lastEnvironmentId: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[Keys.LAST_ENV_ID] ?: 0L
    }

    suspend fun saveLastEnvironment(id: Long) {
        context.dataStore.edit { preferences ->
            preferences[Keys.LAST_ENV_ID] = id
        }
    }
}