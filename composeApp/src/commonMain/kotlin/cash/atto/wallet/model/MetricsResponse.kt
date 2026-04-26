package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class MetricsResponse(
    val metrics: List<Metric>,
)
