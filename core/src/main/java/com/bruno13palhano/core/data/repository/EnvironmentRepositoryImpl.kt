package com.bruno13palhano.core.data.repository

import com.bruno13palhano.core.data.database.dao.EnvironmentDao
import com.bruno13palhano.core.data.database.entity.toDomain
import com.bruno13palhano.core.data.database.entity.toEntity
import com.bruno13palhano.core.model.Environment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

internal class EnvironmentRepositoryImpl @Inject constructor(
    private val environmentDao: EnvironmentDao
) : EnvironmentRepository {
    override suspend fun insert(environment: Environment) {
        environmentDao.insert(entity = environment.toEntity())
    }

    override suspend fun update(environment: Environment) {
        environmentDao.update(entity = environment.toEntity())
    }

    override suspend fun deleteById(id: Long) {
        environmentDao.deleteById(id = id)
    }

    override fun getAll(): Flow<List<Environment>> {
        return environmentDao.getAll().map { it.map { entity -> entity.toDomain() } }
    }

    override suspend fun getById(id: Long): Environment? {
        return environmentDao.getById(id = id)?.toDomain()
    }

    override suspend fun getLast(): Environment? {
        return environmentDao.getLast()?.toDomain()
    }
}