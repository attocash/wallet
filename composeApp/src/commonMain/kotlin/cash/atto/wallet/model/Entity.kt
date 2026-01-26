package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class Entity(
    val entity: String,
    val organization: String,
    val label: String,
    val website: String,
    val tags: List<String>,
    val addedAt: String,
    val description: String
)
