package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val address: String,
    val label: String,
    val entity: String,
    val addedAt: String,
    val description: String
)
