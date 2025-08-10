package com.bruno13palhano.core.data.network

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

    suspend fun publish(topic: String, message: String): Result<Unit> = withContext(Dispatchers.IO) {
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

    suspend fun connect(mqttConnectionConfig: MqttConnectionConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (mqttConnectionConfig.caBytes?.isEmpty() == true || mqttConnectionConfig.clientP12Bytes?.isEmpty() == true) {
                return@withContext Result.failure(IllegalArgumentException("Certificate or key is empty"))
            }

            // 1) Prepare TMF (with CA provided or system)
            val tmf = loadTrustManagerFactoryFromCaBytes(caBytes = mqttConnectionConfig.caBytes)

            // 2) If there is p12, try to use mutual TLS (client certificate)
            if (mqttConnectionConfig.clientP12Bytes != null && mqttConnectionConfig.p12Password != null) {
                try {
                    val kmf = loadKeyManagerFactoryFromP12Bytes(
                        p12Bytes = mqttConnectionConfig.clientP12Bytes,
                        p12Password = mqttConnectionConfig.p12Password
                    )
                    val sslConfig = buildSslConfig(tmf = tmf, kmf = kmf)

                    client = MqttClient.builder()
                        .identifier(mqttConnectionConfig.clientId)
                        .serverHost(mqttConnectionConfig.host)
                        .serverPort(mqttConnectionConfig.port)
                        .sslConfig(sslConfig)
                        .useMqttVersion5()
                        .buildAsync()

                    // try to connect without username/password (mutual TLS)
                    client.connectWith()
                        .send()
                        .await()

                    return@withContext Result.success(Unit)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val sslConfig = buildSslConfig(tmf, null)

            client = MqttClient.builder()
                .identifier(mqttConnectionConfig.clientId)
                .serverHost(mqttConnectionConfig.host)
                .serverPort(mqttConnectionConfig.port)
                .sslConfig(sslConfig)
                .useMqttVersion5()
                .buildAsync()

            val connectBuilder = client.connectWith()
            if (!mqttConnectionConfig.username.isNullOrBlank() && mqttConnectionConfig.password != null) {
                connectBuilder.simpleAuth()
                    .username(mqttConnectionConfig.username)
                    .password(mqttConnectionConfig.password.toByteArray())
                    .applySimpleAuth()
            }
            connectBuilder.send().await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Establishes a connection to an MQTT server using the provided configuration.
     * Supports TLS authentication with a CA certificate and mutual authentication with a PKCS12 file,
     * both provided as [ByteArray]. If no CA certificate is provided, the system's default truststore
     * will be used. If a PKCS12 file is provided, attempts mutual TLS authentication; otherwise,
     * uses username and password authentication, if provided.
     *
     * @param clientId Unique identifier for the MQTT client.
     * @param host Address of the MQTT server.
     * @param port Port of the MQTT server (e.g., 1883 for non-TLS, 8883 for TLS).
     * @param username Username for authentication (optional).
     * @param password Password for authentication (optional, requires [username]).
     * @param caBytes CA certificate in X.509 DER format to configure the [TrustManagerFactory] (optional).
     * @param clientP12Bytes PKCS12 file containing the client certificate and private key (optional).
     * @param p12Password Password to unlock the PKCS12 file (required if [clientP12Bytes] is provided).
     * @return [Result]<[Unit]> indicating success or failure of the connection.
     * @throws Exception If an error occurs during configuration or connection to the MQTT server.
     */
    suspend fun connect(
        clientId: String,
        host: String,
        port: Int,
        username: String?,
        password: String?,
        caBytes: ByteArray?,
        clientP12Bytes: ByteArray?,
        p12Password: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (caBytes?.isEmpty() == true || clientP12Bytes?.isEmpty() == true) {
                return@withContext Result.failure(IllegalArgumentException("Certificate or key is empty"))
            }

            // 1) Prepare TMF (with CA provided or system)
            val tmf = loadTrustManagerFactoryFromCaBytes(caBytes = caBytes)

            // 2) If there is p12, try to use mutual TLS (client certificate)
            if (clientP12Bytes != null && p12Password != null) {
                try {
                    val kmf = loadKeyManagerFactoryFromP12Bytes(p12Bytes = clientP12Bytes, p12Password = p12Password)
                    val sslConfig = buildSslConfig(tmf = tmf, kmf = kmf)

                    client = MqttClient.builder()
                        .identifier(clientId)
                        .serverHost(host)
                        .serverPort(port)
                        .sslConfig(sslConfig)
                        .useMqttVersion5()
                        .buildAsync()

                    // try to connect without username/password (mutual TLS)
                    client.connectWith()
                        .send()
                        .await()

                    return@withContext Result.success(Unit)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val sslConfig = buildSslConfig(tmf, null)

            client = MqttClient.builder()
                .identifier(clientId)
                .serverHost(host)
                .serverPort(port)
                .sslConfig(sslConfig)
                .useMqttVersion5()
                .buildAsync()

            val connectBuilder = client.connectWith()
            if (!username.isNullOrBlank() && password != null) {
                connectBuilder.simpleAuth()
                    .username(username)
                    .password(password.toByteArray())
                    .applySimpleAuth()
            }
            connectBuilder.send().await()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
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
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply { load(null, null) }
        ByteArrayInputStream(caBytes).use { stream ->
            val caCert = cf.generateCertificate(stream)
            keyStore.setCertificateEntry("ca", caCert)
        }
        tmf.init(keyStore)
        return tmf
    }

    @Throws(Exception::class)
    private fun loadKeyManagerFactoryFromP12Bytes(p12Bytes: ByteArray, p12Password: String): KeyManagerFactory {
        // PKCS12 keystore that contains client cert + private key
        val keyStore = KeyStore.getInstance("PKCS12")
        ByteArrayInputStream(p12Bytes).use { stream ->
            keyStore.load(stream, p12Password.toCharArray())
        }
        val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
        kmf.init(keyStore, p12Password.toCharArray())
        return kmf
    }

    private fun buildSslConfig(tmf: TrustManagerFactory, kmf: KeyManagerFactory?): MqttClientSslConfig {
        val builder = MqttClientSslConfig.builder()
            .trustManagerFactory(tmf)
        // if there is KeyManager, add
        kmf?.let { builder.keyManagerFactory(it) }

        // For development, accepts any hostname (not recommended in production)
        return builder.hostnameVerifier { _, _ -> true }.build()
    }
}