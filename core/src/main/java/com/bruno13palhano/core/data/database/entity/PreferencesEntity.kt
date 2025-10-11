package com.bruno13palhano.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bruno13palhano.core.model.Preferences

@Entity(tableName = "preferences")
internal data class PreferencesEntity(
    @PrimaryKey
    var id: Long = 1L,
    val lastEnvironmentId: Long,
)

internal fun PreferencesEntity.toDomain(): Preferences =
    Preferences(lastEnvironmentId = lastEnvironmentId)

internal fun Preferences.toEntity(): PreferencesEntity =
    PreferencesEntity(lastEnvironmentId = lastEnvironmentId)
