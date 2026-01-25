package cash.atto.wallet.repository

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.model.HomeResponse
import cash.atto.wallet.model.getPriceUsd
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.minutes

class HomeRepository(
    private val network: AttoNetwork
) {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val _homeResponse = MutableStateFlow<HomeResponse?>(null)
    val homeResponse = _homeResponse.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            while (true) {
                fetchHome()
                delay(1.minutes)
            }
        }
    }

    suspend fun fetchHome(): HomeResponse? {
        val baseUrl = network.gatekeerperUrl()
        return try {
            val response = client.get("${baseUrl}/projections/home")
            val homeResponse = response.body<HomeResponse>()
            _homeResponse.emit(homeResponse)
            homeResponse
        } catch (e: Exception) {
            println("Error fetching home: ${e.message} ${e.stackTraceToString()}")
            null
        }
    }

    fun getPriceUsd(): BigDecimal? {
        return homeResponse.value?.getPriceUsd()?.let {
            BigDecimal.parseString(it)
        }
    }
}
