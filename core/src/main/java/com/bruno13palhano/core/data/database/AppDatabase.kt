package com.bruno13palhano.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bruno13palhano.core.data.database.dao.EnvironmentDao
import com.bruno13palhano.core.data.database.dao.MqttConnectionConfigDao
import com.bruno13palhano.core.data.database.dao.PreferencesDao
import com.bruno13palhano.core.data.database.dao.WidgetDao
import com.bruno13palhano.core.data.database.entity.EnvironmentEntity
import com.bruno13palhano.core.data.database.entity.MqttConnectionConfigEntity
import com.bruno13palhano.core.data.database.entity.PreferencesEntity
import com.bruno13palhano.core.data.database.entity.WidgetEntity

@Database(
    entities = [
        WidgetEntity::class,
        MqttConnectionConfigEntity::class,
        EnvironmentEntity::class,
        PreferencesEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
@TypeConverters(value = [SecureConverters::class, ExtrasConverters::class])
internal abstract class AppDatabase : RoomDatabase() {
    abstract fun widgetDao(): WidgetDao
    abstract fun mqttConnectionConfigDao(): MqttConnectionConfigDao
    abstract fun environmentDao(): EnvironmentDao
    abstract fun preferencesDao(): PreferencesDao
}
