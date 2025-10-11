package com.bruno13palhano.hmiapp.ui.factory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dagger.hilt.android.EntryPointAccessors

@Composable
inline fun <reified VM : ViewModel, S : Any, EP : Any> assistedViewModel(
    state: S,
    entryPoint: Class<EP>,
    crossinline factorySelector: (EP, S) -> VM,
): VM {
    val context = LocalContext.current.applicationContext
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)

    return remember(state) {
        val ep = EntryPointAccessors.fromApplication(context, entryPoint)
        val viewModel = factorySelector(ep, state)

        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T = viewModel as T
        }

        ViewModelProvider(viewModelStoreOwner, factory)[VM::class.java]
    }
}
