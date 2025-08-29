package com.bruno13palhano.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bruno13palhano.core.data.database.entity.WidgetEntity

@Dao
internal interface WidgetDao {
    @Query("SELECT * FROM widgets WHERE environmentId = :environmentId")
    suspend fun getWidgets(environmentId: Long): List<WidgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: WidgetEntity)

    @Update
    suspend fun update(entity: WidgetEntity)

    @Query("DELETE FROM widgets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE widgets SET x = :x, y = :y WHERE id = :id")
    suspend fun updatePosition(id: String, x: Float, y: Float)
}