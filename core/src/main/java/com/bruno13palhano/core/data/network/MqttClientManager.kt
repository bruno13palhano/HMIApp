package com.bruno13palhano.core.data.network

import android.util.Log
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientSslConfig
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.TrustManagerFactory

@Singleton
internal class MqttClientManager @Inject constructor() {
    private lateinit var client: Mqtt5AsyncClient

    private val _incomingMessages = MutableSharedFlow<Pair<String, String>>(
        replay = 0,
        extraBufferCapacity = 12,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingMessages: SharedFlow<Pair<String, String>> = _incomingMessages.asSharedFlow()

    suspend fun subscribe(topic: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.subscribeWith()
                .topicFilter(topic)
                .callback { publish ->
                    val message = String(publish.payloadAsBytes)
                        _incomingMessages.tryEmit(topic to message)
                }
                .send()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun publish(topic: String, message: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                client.publishWith()
                    .topic(topic)
                    .payload(message.toByteArray())
                    .send()
                    .await()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun isConnected(): Flow<Boolean> {
        return flow {
            while (true) {
                try {
                    emit(client.state.isConnected)
                } catch (_: Exception) {
                    emit(false)
                }
                delay(250)
            }
        }.distinctUntilChanged()
    }

    suspend fun disconnect(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            client.disconnect().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> CompletableFuture<T>.await() = withContext(Dispatchers.IO) {
        try {
            get()
        } catch (e: Exception) {
            throw e
        }
    }



//    @Throws(Exception::class)
//    private suspend fun connectWithoutSsl(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> {
//        try {
//            client = MqttClient.builder()
//                .identifier(mqttConnectionConfig.clientId)
//                .serverHost(mqttConnectionConfig.host)
//                .serverPort(mqttConnectionConfig.port)
//                .useMqttVersion5()
//                .buildAsync()
//
//            val connectBuilder = client.connectWith()
//            if (!mqttConnectionConfig.username.isNullOrBlank() && mqttConnectionConfig.password != null) {
//                connectBuilder.simpleAuth()
//                    .username(mqttConnectionConfig.username)
//                    .password(mqttConnectionConfig.password.toByteArray())
//                    .applySimpleAuth()
//            }
//
//            connectBuilder.send().await()
//            return Result.success(Unit)
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//
//    @Throws(Exception::class)
//    private suspend fun connectWithMutualTls(
//        mqttConnectionConfig: MqttConnectionConfig,
//        sslConfig: MqttClientSslConfig
//    ): Result<Unit> {
//        try {
//            client = MqttClient.builder()
//                .identifier(mqttConnectionConfig.clientId)
//                .serverHost(mqttConnectionConfig.host)
//                .serverPort(mqttConnectionConfig.port)
//                .sslConfig(sslConfig)
//                .useMqttVersion5()
//                .buildAsync()
//
//            if (mqttConnectionConfig.username != null && mqttConnectionConfig.password != null) {
//                // try to connect with username/password (mutual TLS)
//                client.connectWith()
//                    .simpleAuth()
//                    .username(mqttConnectionConfig.username)
//                    .password(mqttConnectionConfig.password.toByteArray())
//                    .applySimpleAuth()
//                    .send()
//                    .await()
//
//                return Result.success(Unit)
//            }
//
//            // try to connect without username/password (mutual TLS)
//            client.connectWith()
//                .send()
//                .await()
//
//            return Result.success(Unit)
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//
//    @Throws(Exception::class)
//    suspend fun connectWithCredentialsOrAnonymous(
//        mqttConnectionConfig: MqttConnectionConfig,
//        sslConfig: MqttClientSslConfig
//    ): Result<Unit> {
//        try {
//            client = MqttClient.builder()
//                .identifier(mqttConnectionConfig.clientId)
//                .serverHost(mqttConnectionConfig.host)
//                .serverPort(mqttConnectionConfig.port)
//                .sslConfig(sslConfig)
//                .useMqttVersion5()
//                .buildAsync()
//
//            val connectBuilder = client.connectWith()
//            if (!mqttConnectionConfig.username.isNullOrBlank() &&
//                mqttConnectionConfig.password != null
//            ) {
//                connectBuilder.simpleAuth()
//                    .username(mqttConnectionConfig.username)
//                    .password(mqttConnectionConfig.password.toByteArray())
//                    .applySimpleAuth()
//            }
//            connectBuilder.send().await()
//            return Result.success(Unit)
//        } catch (e: Exception) {
//            throw e
//        }
//    }
//
//    suspend fun connect(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> =
//        withContext(Dispatchers.IO) {
//            try {
//                // Try to connect without ssl if the broker allows it
//                if (mqttConnectionConfig.caBytes == null
//                    || mqttConnectionConfig.caBytes.isEmpty()
//                ) {
//                    try {
//                        connectWithoutSsl(mqttConnectionConfig = mqttConnectionConfig)
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                        return@withContext Result.failure(e)
//                    }
//                }
//
//                if (mqttConnectionConfig.clientP12Bytes?.isEmpty() == true) {
//                    return@withContext Result.failure(
//                        IllegalArgumentException("Key is empty")
//                    )
//                }
//
//                // 1) Prepare TMF (with CA provided or system)
//                val tmf = loadTrustManagerFactoryFromCaBytes(caBytes = mqttConnectionConfig.caBytes)
//
//                // 2) If there is p12, try to use mutual TLS (client certificate)
//                if (mqttConnectionConfig.clientP12Bytes != null &&
//                    mqttConnectionConfig.p12Password != null
//                ) {
//                    try {
//                        val kmf = loadKeyManagerFactoryFromP12Bytes(
//                            p12Bytes = mqttConnectionConfig.clientP12Bytes,
//                            p12Password = mqttConnectionConfig.p12Password
//                        )
//                        val sslConfig = buildSslConfig(tmf = tmf, kmf = kmf)
//
//                        return@withContext connectWithMutualTls(
//                            mqttConnectionConfig = mqttConnectionConfig,
//                            sslConfig = sslConfig
//                        )
//                    } catch (e: Exception) {
//                        e.printStackTrace()
//                    }
//                }
//
//                val sslConfig = buildSslConfig(tmf, null)
//
//                connectWithCredentialsOrAnonymous(
//                    mqttConnectionConfig = mqttConnectionConfig,
//                    sslConfig = sslConfig
//                )
//            } catch (e: Exception) {
//                e.printStackTrace()
//                Result.failure(e)
//            }
//        }


    suspend fun connect(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                Log.d(
                    "MqttClientManager",
                    "Starting connection to host: ${mqttConnectionConfig.host}:" +
                            "${mqttConnectionConfig.port}"
                )
                if (mqttConnectionConfig.caBytes == null
                    || mqttConnectionConfig.caBytes.isEmpty()
                ) {
                    try {
                        Log.w(
                            "MqttClientManager",
                            "Attempting connection without TLS"
                        )
                        return@withContext connectWithoutSsl(
                            mqttConnectionConfig = mqttConnectionConfig
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return@withContext Result.failure(e)
                    }
                }

                val tmf = loadTrustManagerFactoryFromCaBytes(caBytes = mqttConnectionConfig.caBytes)

                if (mqttConnectionConfig.clientP12Bytes != null &&
                    mqttConnectionConfig.p12Password != null
                ) {
                    if (mqttConnectionConfig.clientP12Bytes.isEmpty()) {
                        Log.e("MqttClientManager", "clientP12Bytes is empty")
                        return@withContext Result.failure(
                            IllegalArgumentException("Client P12 bytes are empty")
                        )
                    }

                    try {
                    Log.d("MqttClientManager", "Attempting connection with mutual TLS")
                    val kmf = loadKeyManagerFactoryFromP12Bytes(
                        p12Bytes = mqttConnectionConfig.clientP12Bytes,
                        p12Password = mqttConnectionConfig.p12Password
                    )
                    val sslConfig = buildSslConfig(tmf = tmf, kmf = kmf)

                    return@withContext connectWithMutualTls(
                        mqttConnectionConfig = mqttConnectionConfig,
                        sslConfig = sslConfig
                    )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                Log.d("MqttClientManager", "Attempting connection with simple TLS")
                val sslConfig = buildSslConfig(tmf, null)

                connectWithCredentialsOrAnonymous(
                    mqttConnectionConfig = mqttConnectionConfig,
                    sslConfig = sslConfig
                )
            } catch (e: Exception) {
                Log.e("MqttClientManager", "Connection failed: ${e.message}", e)
                Result.failure(e)
            }
        }

    private fun buildMqttClient(
        mqttConnectionConfig: MqttConnectionConfig,
        sslConfig: MqttClientSslConfig? = null
    ): Mqtt5AsyncClient {
        return MqttClient.builder()
            .identifier(mqttConnectionConfig.clientId)
            .serverHost(mqttConnectionConfig.host)
            .serverPort(mqttConnectionConfig.port)
            .sslConfig(sslConfig)
            .useMqttVersion5()
            .buildAsync()
    }

    @Throws(Exception::class)
    private suspend fun connectWithoutSsl(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> {
        client = buildMqttClient(mqttConnectionConfig = mqttConnectionConfig)

        val connectBuilder = client.connectWith()
        if (!mqttConnectionConfig.username.isNullOrBlank() && mqttConnectionConfig.password != null) {
            connectBuilder.simpleAuth()
                .username(mqttConnectionConfig.username)
                .password(mqttConnectionConfig.password.toByteArray(StandardCharsets.UTF_8))
                .applySimpleAuth()
        }

        connectBuilder.send().await()
        Log.d("MqttClientManager", "Connection successful")

        return Result.success(Unit)
    }

    @Throws(Exception::class)
    private suspend fun connectWithMutualTls(
        mqttConnectionConfig: MqttConnectionConfig,
        sslConfig: MqttClientSslConfig
    ): Result<Unit> {
        client = buildMqttClient(
            mqttConnectionConfig = mqttConnectionConfig,
            sslConfig = sslConfig
        )

        if (mqttConnectionConfig.username != null && mqttConnectionConfig.password != null) {
            // try to connect with username/password (mutual TLS)
            client.connectWith()
                .simpleAuth()
                .username(mqttConnectionConfig.username)
                .password(mqttConnectionConfig.password.toByteArray(StandardCharsets.UTF_8))
                .applySimpleAuth()
        }

        // try to connect without username/password (mutual TLS)
        client.connectWith().send().await()
        Log.d("MqttClientManager", "Connection successful")

        return Result.success(Unit)
    }

    @Throws(Exception::class)
    suspend fun connectWithCredentialsOrAnonymous(
        mqttConnectionConfig: MqttConnectionConfig,
        sslConfig: MqttClientSslConfig
    ): Result<Unit> {
        client = buildMqttClient(
            mqttConnectionConfig = mqttConnectionConfig,
            sslConfig = sslConfig
        )

        val connectBuilder = client.connectWith()
        if (!mqttConnectionConfig.username.isNullOrBlank() &&
            mqttConnectionConfig.password != null
        ) {
            connectBuilder.simpleAuth()
                .username(mqttConnectionConfig.username)
                .password(mqttConnectionConfig.password.toByteArray(StandardCharsets.UTF_8))
                .applySimpleAuth()
        }

        connectBuilder.send().await()
        Log.d("MqttClientManager", "Connection successful")

        return Result.success(Unit)
    }

    @Throws(Exception::class)
    private fun loadTrustManagerFactoryFromCaBytes(caBytes: ByteArray?): TrustManagerFactory {
        val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
        val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)

        if (caBytes == null) {
            // use system default truststore
            tmf.init(null as KeyStore?)
            return tmf
        }

        val cf = CertificateFactory.getInstance("X.509")
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
            load(null, null)
        }
        ByteArrayInputStream(caBytes).use { stream ->
            val caCert = cf.generateCertificate(stream)
            keyStore.setCertificateEntry("ca", caCert)
        }
        tmf.init(keyStore)
        return tmf
    }

    @Throws(Exception::class)
    private fun loadKeyManagerFactoryFromP12Bytes(
        p12Bytes: ByteArray,
        p12Password: String
    ): KeyManagerFactory {
        // PKCS12 keystore that contains client cert + private key
        val keyStore = KeyStore.getInstance("PKCS12")
        ByteArrayInputStream(p12Bytes).use { stream ->
            keyStore.load(stream, p12Password.toCharArray())
        }
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, p12Password.toCharArray())
        return kmf
    }

    private fun buildSslConfig(
        tmf: TrustManagerFactory,
        kmf: KeyManagerFactory?
    ): MqttClientSslConfig {
        val builder = MqttClientSslConfig.builder()
            .trustManagerFactory(tmf)
        kmf?.let { builder.keyManagerFactory(it) }

        return builder
            // For development, accepts any hostname
            //.hostnameVerifier { _, _ -> true }
            .build()
    }
}