package com.bruno13palhano.core.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bruno13palhano.core.model.Environment

@Entity(tableName = "environments")
data class EnvironmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
)

internal fun EnvironmentEntity.toDomain() = Environment(
    id = id,
    name = name,
    scale = scale,
    offsetX = offsetX,
    offsetY = offsetY
)

internal fun Environment.toEntity() = EnvironmentEntity(
    id = id,
    name = name,
    scale = scale,
    offsetX = offsetX,
    offsetY = offsetY
)