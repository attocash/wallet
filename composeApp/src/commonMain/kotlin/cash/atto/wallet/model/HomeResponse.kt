package cash.atto.wallet.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeResponse(
    val metrics: List<Metric>,
    val addresses: List<Address> = emptyList(),
    val voters: List<Voter> = emptyList(),
) {
    val metricMap = metrics.associateBy { it.name }
    val addressMap = addresses.associateBy { it.address }
    val voterMap = voters.associateBy { it.address }
}


fun HomeResponse.getPriceUsd(): String? = metrics.getPriceUsd()

fun HomeResponse.getStakingApy(): String? = metrics.getStakingApy()

fun HomeResponse.getVoter(address: String): Voter? = voterMap[address]

fun HomeResponse.getAddressLabel(address: String): String? = addressMap[address]?.label

fun HomeResponse.getVoterLabel(address: String): String? = voterMap[address]?.label
