package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val value: String,
    val name: String,
    val date: String
)

fun List<Metric>.getMetricValue(name: String): String? {
    return firstOrNull { it.name == name }?.value
}

fun List<Metric>.getPriceUsd(): String? {
    return getMetricValue("price.usd")
}

fun List<Metric>.getCirculatingSupply(): String? {
    return getMetricValue("supply.circulating")
}

fun List<Metric>.getAverageConfirmationMs(): String? {
    return getMetricValue("network.confirmation-time.ms.average")
}

fun List<Metric>.getStakingApy(): String? {
    return getMetricValue("distribution.staking.annual-percentage-yield.effective")
}
