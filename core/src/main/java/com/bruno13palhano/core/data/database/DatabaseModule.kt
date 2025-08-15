package com.bruno13palhano.core.data.database

import android.content.Context
import androidx.room.Room
import com.bruno13palhano.core.data.database.dao.EnvironmentDao
import com.bruno13palhano.core.data.database.dao.MqttConnectionConfigDao
import com.bruno13palhano.core.data.database.dao.WidgetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "hmi_db"
        )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideWidgetDao(db: AppDatabase): WidgetDao = db.widgetDao()

    @Provides
    fun provideMqttConnectionConfigDao(db: AppDatabase): MqttConnectionConfigDao = db.mqttConnectionConfigDao()

    @Provides
    fun provideEnvironmentDao(db: AppDatabase): EnvironmentDao = db.environmentDao()
}