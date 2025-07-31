package com.bruno13palhano.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bruno13palhano.core.data.database.dao.WidgetDao
import com.bruno13palhano.core.data.database.entity.WidgetEntity

@Database(
    entities = [WidgetEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun widgetDao(): WidgetDao
}