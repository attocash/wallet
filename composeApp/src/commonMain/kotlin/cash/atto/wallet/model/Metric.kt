package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val value: String,
    val name: String,
    val date: String
)
