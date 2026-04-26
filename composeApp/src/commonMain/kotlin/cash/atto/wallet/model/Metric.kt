package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class Metric(
    val value: String,
    val name: String,
    val date: String,
)

fun List<Metric>.getMetricValue(name: String): String? = firstOrNull { it.name == name }?.value

fun List<Metric>.getPriceUsd(): String? = getMetricValue("price.usd")

fun List<Metric>.getCirculatingSupply(): String? = getMetricValue("supply.circulating")

fun List<Metric>.getConfirmationMs(): String? = getMetricValue("network.confirmation-time.ms.p95")

fun List<Metric>.getStakingApy(): String? = getMetricValue("distribution.staking.annual-percentage-yield.effective")
