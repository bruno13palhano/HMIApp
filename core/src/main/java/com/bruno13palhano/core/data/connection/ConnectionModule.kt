package com.bruno13palhano.core.data.connection

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ConnectionModule {
    @Binds
    abstract fun bindConnectionSession(connectionSession: ConnectionSessionImpl): ConnectionSession
}