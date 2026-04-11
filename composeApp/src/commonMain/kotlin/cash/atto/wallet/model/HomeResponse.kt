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
    return metrics.getMetricValue(name)
}

fun HomeResponse.getPriceUsd(): String? {
    return metrics.getPriceUsd()
}

fun HomeResponse.getCirculatingSupply(): String? {
    return metrics.getCirculatingSupply()
}

fun HomeResponse.getAverageConfirmationMs(): String? {
    return metrics.getAverageConfirmationMs()
}

fun HomeResponse.getStakingApy(): String? {
    return metrics.getStakingApy()
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
