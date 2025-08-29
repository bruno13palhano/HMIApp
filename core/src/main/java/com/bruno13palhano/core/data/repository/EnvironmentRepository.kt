package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.model.Environment
import kotlinx.coroutines.flow.Flow

interface EnvironmentRepository {
    suspend fun insert(environment: Environment)
    suspend fun update(environment: Environment)
    suspend fun deleteById(id: Long)
    fun getAll(): Flow<List<Environment>>
    suspend fun getById(id: Long): Environment?
    suspend fun getLast(): Environment?
    suspend fun getLastEnvironmentId(): Long?
    suspend fun setLastEnvironmentId(id: Long)
}