package com.bruno13palhano.hmiapp.ui.shared

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class Container<STATE, EFFECT>(
    initialSTATE: STATE,
    private val scope: CoroutineScope,
) {
    private val _state: MutableStateFlow<STATE> = MutableStateFlow(initialSTATE)
    val state: StateFlow<STATE> = _state.asStateFlow()

    private val _sideEffect = Channel<EFFECT>(Channel.CONFLATED)
    val sideEffect: Flow<EFFECT> = _sideEffect.receiveAsFlow()

    fun intent(transform: suspend Container<STATE, EFFECT>.() -> Unit) {
        scope.launch {
            this@Container.transform()
        }
    }

    suspend fun reduce(reducer: suspend STATE.() -> STATE) {
        _state.value = _state.value.reducer()
    }

    suspend fun postSideEffect(effect: EFFECT) {
        _sideEffect.send(effect)
    }
}