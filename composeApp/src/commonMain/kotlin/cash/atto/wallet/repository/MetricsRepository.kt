package cash.atto.wallet.repository

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.model.MetricsResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

class MetricsRepository(
    private val network: AttoNetwork,
) {
    private val client =
        HttpClient {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }

    private val _metricsResponse = MutableStateFlow<MetricsResponse?>(null)
    val metricsResponse = _metricsResponse.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            while (true) {
                fetchMetrics()
                delay(1.minutes)
            }
        }
    }

    suspend fun fetchMetrics(): MetricsResponse? {
        val baseUrl = network.gatekeerperUrl()
        val response = fetchMetricsProjection(baseUrl)
        response?.let { _metricsResponse.emit(it) }
        return response
    }

    private suspend fun fetchMetricsProjection(baseUrl: String): MetricsResponse? =
        try {
            client.get("$baseUrl/projections/metrics").body<MetricsResponse>()
        } catch (e: Exception) {
            println("Error fetching metrics projection: ${e.message} ${e.stackTraceToString()}")
            null
        }
}
