package cash.atto.wallet.repository

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.model.VotersResponse
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
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

class VotersRepository(
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

    private val _votersResponse = MutableStateFlow<VotersResponse?>(null)
    val votersResponse = _votersResponse.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            while (true) {
                fetchVoters()
                delay(1.minutes)
            }
        }
    }

    suspend fun fetchVoters(): VotersResponse? {
        val baseUrl = network.gatekeerperUrl()
        return try {
            val response = client.get("${baseUrl}/projections/voters")
            val votersResponse = response.body<VotersResponse>()
            _votersResponse.emit(votersResponse)
            votersResponse
        } catch (e: Exception) {
            println("Error fetching voters: ${e.message}")
            null
        }
    }

    fun getVoterLabel(address: String): String? {
        return votersResponse.value?.voters?.find {
            it.address == address
        }?.label
    }

    fun calculateUserApy(voterAddress: String): BigDecimal? {
        val response = votersResponse.value ?: return null
        val voter = response.voters.find { it.address == voterAddress } ?: return null

        val globalApy = BigDecimal.parseString(response.apy)

        val sharePercentage = BigDecimal.fromInt(voter.sharePercentage)
        val hundred = BigDecimal.fromInt(100)

        val mode = DecimalMode(
            decimalPrecision = 5,
            scale = 2,
            roundingMode = RoundingMode.ROUND_HALF_CEILING
        )

        return (globalApy * sharePercentage).divide(hundred, decimalMode = mode)
    }
}
