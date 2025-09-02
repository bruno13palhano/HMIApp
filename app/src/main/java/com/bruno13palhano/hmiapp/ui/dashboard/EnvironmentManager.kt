package com.bruno13palhano.hmiapp.ui.dashboard

import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.core.model.Environment
import kotlinx.coroutines.flow.Flow

class EnvironmentManager(
    private val environmentRepository: EnvironmentRepository
) {
    suspend fun addEnvironment(environment: Environment): Environment? {
        environmentRepository.insert(environment = environment)
        return environmentRepository.getLast()
    }

    suspend fun editEnvironment(environment: Environment) {
        environmentRepository.update(environment = environment)
    }

    suspend fun changeEnvironment(id: Long): Environment? {
        val environment = environmentRepository.getById(id = id)
        environment?.let { environmentRepository.setLastEnvironmentId(id = it.id) }
        return environment
    }

    fun loadEnvironments(): Flow<List<Environment>> {
        return environmentRepository.getAll()
    }

    suspend fun loadPreviousEnvironment(): Environment? {
        val id = environmentRepository.getLastEnvironmentId()
        if (id == null) return null

        val environment = environmentRepository.getById(id = id)
        return environment
    }

    suspend fun updateEnvironmentState(environment: Environment) {
        if (environment.id == 0L) return
        environmentRepository.update(environment = environment)
    }
}