package com.bruno13palhano.core.data.database

import android.content.Context
import androidx.room.Room
import com.bruno13palhano.core.data.database.dao.WidgetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
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
}