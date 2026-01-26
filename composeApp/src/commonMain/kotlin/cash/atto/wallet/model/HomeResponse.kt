package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val metrics: List<Metric>,
    val voters: List<Voter>
) {
    val metricMap = metrics.associateBy { it.name }
    val voterMap = voters.associateBy { it.address }
}

@Serializable
data class Metric(
    val value: String,
    val name: String,
    val date: String
)

fun HomeResponse.getMetricValue(name: String): String? {
    return metricMap[name]?.value
}

fun HomeResponse.getPriceUsd(): String? {
    return getMetricValue("price.usd")
}

fun HomeResponse.getStakingApy(): String? {
    return getMetricValue("distribution.staking.annual-percentage-yield.effective")
}

fun HomeResponse.getVoter(address: String): Voter? {
    return voterMap[address]
}