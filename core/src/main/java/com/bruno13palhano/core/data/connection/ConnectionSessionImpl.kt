package com.bruno13palhano.core.data.connection

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import kotlinx.serialization.json.Json

private const val TOKEN_FILE_NAME = "HMI"

@Singleton
internal class ConnectionSessionImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ConnectionSession {
    companion object{
        const val CONNECTION_JSON = "CONNECTION_JSON"
    }

    private var preferences: SharedPreferences = context.getSharedPreferences(
        TOKEN_FILE_NAME,
        Context.MODE_PRIVATE
    )

    override fun save(connection: Connection) {
        preferences.edit {
            putString(CONNECTION_JSON, Json.encodeToString(connection))
        }
    }

    override fun get(): Connection? {
        val json = preferences.getString(CONNECTION_JSON, null) ?: return null
        return runCatching { Json.decodeFromString<Connection>(json) }.getOrNull()
    }

    override fun clear() {
        preferences.edit { clear() }
    }
}