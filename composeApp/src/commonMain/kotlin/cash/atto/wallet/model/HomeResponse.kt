package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val metrics: List<Metric>,
    val voters: List<Voter>
)

@Serializable
data class Metric(
    val value: String,
    val name: String,
    val date: String
)

fun HomeResponse.getMetricValue(name: String): String? {
    return metrics.find { it.name == name }?.value
}

fun HomeResponse.getPriceUsd(): String? {
    return getMetricValue("price.usd")
}
