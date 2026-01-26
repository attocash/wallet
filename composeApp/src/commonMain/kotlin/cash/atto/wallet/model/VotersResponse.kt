package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class VotersResponse(
    val apy: String,
    val voters: List<Voter>,
    val entities: List<Entity>
)
