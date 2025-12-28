package cash.atto.wallet.repository

import cash.atto.commons.AttoNetwork
import cash.atto.wallet.model.VotersResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

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

    private fun getBaseUrl(): String {
        return when (network) {
            AttoNetwork.LOCAL -> "https://gatekeeper.dev.application.atto.cash"
            AttoNetwork.DEV -> "https://gatekeeper.dev.application.atto.cash"
            AttoNetwork.BETA -> "https://gatekeeper.beta.application.atto.cash"
            AttoNetwork.LIVE -> "https://gatekeeper.live.application.atto.cash"
            else -> throw IllegalArgumentException("Unsupported network: $network")
        }
    }

    suspend fun fetchVoters(): VotersResponse? {
        return try {
            val response = client.get("${getBaseUrl()}/projections/voters")
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

    fun calculateUserApy(voterAddress: String): Double? {
        val response = votersResponse.value ?: return null
        val voter = response.voters.find { it.address == voterAddress } ?: return null
        val globalApy = response.apy.toDoubleOrNull() ?: return null
        return globalApy * voter.sharePercentage / 100.0
    }
}
