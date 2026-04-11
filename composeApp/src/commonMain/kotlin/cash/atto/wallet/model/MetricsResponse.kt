package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class MetricsResponse(
    val metrics: List<Metric>
)

fun MetricsResponse.getMetricValue(name: String): String? {
    return metrics.getMetricValue(name)
}

fun MetricsResponse.getPriceUsd(): String? {
    return metrics.getPriceUsd()
}

fun MetricsResponse.getCirculatingSupply(): String? {
    return metrics.getCirculatingSupply()
}

fun MetricsResponse.getAverageConfirmationMs(): String? {
    return metrics.getAverageConfirmationMs()
}

fun MetricsResponse.getStakingApy(): String? {
    return metrics.getStakingApy()
}
