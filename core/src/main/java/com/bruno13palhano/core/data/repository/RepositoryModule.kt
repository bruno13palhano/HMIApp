package com.bruno13palhano.core.data.repository

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class RepositoryModule {
    @Binds
    abstract fun bindWidgetRepository(repository: WidgetRepositoryImpl): WidgetRepository

    @Binds
    abstract fun bindMqttClientRepository(
        repository: MqttClientRepositoryImpl,
    ): MqttClientRepository

    @Binds
    abstract fun bindEnvironmentRepository(
        repository: EnvironmentRepositoryImpl,
    ): EnvironmentRepository
}
