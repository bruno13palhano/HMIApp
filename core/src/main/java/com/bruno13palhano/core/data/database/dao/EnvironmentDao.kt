package com.bruno13palhano.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bruno13palhano.core.data.database.entity.EnvironmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface EnvironmentDao {
    @Insert
    suspend fun insert(entity: EnvironmentEntity)

    @Update
    suspend fun update(entity: EnvironmentEntity)

    @Query("DELETE FROM environments WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM environments")
    fun getAll(): Flow<List<EnvironmentEntity>>

    @Query("SELECT * FROM environments WHERE id = :id")
    suspend fun getById(id: Long): EnvironmentEntity?

    @Query("SELECT * FROM environments ORDER BY id DESC LIMIT 1")
    suspend fun getLast(): EnvironmentEntity?
}
