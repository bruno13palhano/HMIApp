package com.bruno13palhano.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bruno13palhano.core.data.database.entity.PreferencesEntity

@Dao
internal interface PreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PreferencesEntity): Long

    @Query("SELECT * FROM preferences ORDER BY id DESC LIMIT 1")
    suspend fun getPreferences(): PreferencesEntity?
}