package com.bruno13palhano.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bruno13palhano.core.data.database.entity.MqttConnectionConfigEntity

@Dao
internal interface MqttConnectionConfigDao {
    @Query("SELECT * FROM mqtt_connection LIMIT 1")
    fun getConfig(): MqttConnectionConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(entity: MqttConnectionConfigEntity)

    @Query("DELETE FROM mqtt_connection")
    suspend fun clear()
}
