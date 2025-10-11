package com.bruno13palhano.hmiapp.app

import android.app.Application
import com.bruno13palhano.core.data.repository.MqttClientRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class HMIApp : Application() {
    @Inject lateinit var mqttRepository: MqttClientRepository

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            mqttRepository.connectIfSessionExists()
        }
    }
}
