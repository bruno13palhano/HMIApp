package com.bruno13palhano.core.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class ExtrasConverters {
    @TypeConverter
    fun fromList(value: List<String>?): String? = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String?): List<String>? = if (value == null) {
        null
    } else {
        Json.decodeFromString(value)
    }
}
