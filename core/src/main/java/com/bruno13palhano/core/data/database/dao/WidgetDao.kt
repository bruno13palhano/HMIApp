package com.bruno13palhano.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bruno13palhano.core.data.database.entity.WidgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WidgetDao {
    @Query("SELECT * FROM widgets")
    fun getAll(): Flow<List<WidgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE widgets SET x = :x, y = :y WHERE id = :id")
    suspend fun updatePosition(id: String, x: Float, y: Float)
}