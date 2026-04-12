package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class MetricsResponse(
    val metrics: List<Metric>,
)

fun MetricsResponse.getMetricValue(name: String): String? = metrics.getMetricValue(name)

fun MetricsResponse.getPriceUsd(): String? = metrics.getPriceUsd()

fun MetricsResponse.getCirculatingSupply(): String? = metrics.getCirculatingSupply()

fun MetricsResponse.getAverageConfirmationMs(): String? = metrics.getAverageConfirmationMs()

fun MetricsResponse.getStakingApy(): String? = metrics.getStakingApy()
