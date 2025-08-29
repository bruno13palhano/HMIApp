package com.bruno13palhano.hmiapp.ui.dashboard

import com.bruno13palhano.core.data.repository.EnvironmentRepository
import com.bruno13palhano.hmiapp.ui.shared.Container
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest

class EnvironmentManager(
    private val environmentRepository: EnvironmentRepository,
    private val container: Container<DashboardState, DashboardSideEffect>
) {
    fun addEnvironment(onSuccess: (id: Long) -> Unit) = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isEnvironmentDialogVisible = false) }

        val environment = state.value.environment.copy(
            id = 0L,
            scale = 1f,
            offsetX = 0f,
            offsetY = 0f
        )

        environmentRepository.insert(environment)
        environmentRepository.getLast()?.let {
            reduce { copy(environment = it) }
            onSuccess(it.id)
        }
    }

    fun editEnvironment() = container.intent(dispatcher = Dispatchers.IO) {
        reduce { copy(isEnvironmentDialogVisible = false) }
        val environment = state.value.environment
        environmentRepository.update(environment = environment)
    }

    fun changeEnvironment(id: Long) = container.intent(Dispatchers.IO) {
        environmentRepository.getById(id = id)?.let {
            reduce { copy(environment = it) }
        }
    }

    fun loadEnvironments() = container.intent(dispatcher = Dispatchers.IO) {
        environmentRepository.getAll().collectLatest {
            reduce { copy(environments = it) }
        }
    }

    fun loadPreviousEnvironment(
        onLoadSuccess: (id: Long) -> Unit,
        onFinish: () -> Unit
    ) = container.intent(dispatcher = Dispatchers.IO) {
        environmentRepository.getLastEnvironmentId()?.let { id ->
            environmentRepository.getById(id = id)?.let {
                reduce { copy(environment = it) }

                if (it.id != 0L) onLoadSuccess(it.id)
            }
        }
        onFinish()
    }

    fun updateEnvironmentState(scale: Float, offsetX: Float, offsetY: Float) =
        container.intent(dispatcher = Dispatchers.IO) {
            val currentEnvironment = state.value.environment.copy(
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY
            )
            if (currentEnvironment.id != 0L) {
                environmentRepository.update(environment = currentEnvironment)
            }
        }
}