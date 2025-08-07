package com.bruno13palhano.hmiapp.ui.factory

import androidx.lifecycle.ViewModel

interface AssistedViewModelFactory<S : Any, VM : ViewModel> {
    fun create(state: S): VM
}