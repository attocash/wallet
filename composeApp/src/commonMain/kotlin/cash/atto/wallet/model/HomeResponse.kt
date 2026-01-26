package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val metrics: List<Metric>,
    val addresses: List<Address> = emptyList(),
    val voters: List<Voter> = emptyList()
) {
    val metricMap = metrics.associateBy { it.name }
    val addressMap = addresses.associateBy { it.address }
    val voterMap = voters.associateBy { it.address }
}

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

fun HomeResponse.getAddressLabel(address: String): String? {
    return addressMap[address]?.label
}

fun HomeResponse.getVoterLabel(address: String): String? {
    return voterMap[address]?.label
}