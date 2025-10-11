package com.bruno13palhano.hmiapp.ui.factory

import com.bruno13palhano.hmiapp.ui.dashboard.DashboardViewModelFactory
import com.bruno13palhano.hmiapp.ui.settings.SettingsViewModelFactory
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ViewModelFactoryEntryPoint {
    fun dashboardViewModelFactory(): DashboardViewModelFactory
    fun settingsViewModelFactory(): SettingsViewModelFactory
}
