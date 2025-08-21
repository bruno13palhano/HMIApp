package com.bruno13palhano.hmiapp.ui.shared

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class Container<STATE, EFFECT>(
    initialSTATE: STATE,
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main.immediate
    ),
    private val onError: (Throwable) -> Unit = { it.printStackTrace() }
) {
    private val _state: MutableStateFlow<STATE> = MutableStateFlow(initialSTATE)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val mutex = Mutex()

    private val _sideEffect = MutableSharedFlow<EFFECT>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val sideEffect: Flow<EFFECT> = _sideEffect.asSharedFlow()

    fun intent(
        dispatcher: CoroutineDispatcher = Dispatchers.Main.immediate,
        transform: suspend Container<STATE, EFFECT>.() -> Unit
    ) {
        scope.launch(dispatcher) {
            try {
                this@Container.transform()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    suspend fun intentSync(transform: suspend Container<STATE, EFFECT>.() -> Unit) {
        try {
            this.transform()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            onError(e)
        }
    }

    suspend fun reduce(reducer: suspend STATE.() -> STATE) {
        withContext(Dispatchers.Main.immediate) {
            mutex.withLock {
                _state.value = _state.value.reducer()
            }
        }
    }

    suspend fun postSideEffect(effect: EFFECT) {
        withContext(Dispatchers.Main.immediate) {
            mutex.withLock {
                _sideEffect.emit(effect)
            }
        }
    }
}