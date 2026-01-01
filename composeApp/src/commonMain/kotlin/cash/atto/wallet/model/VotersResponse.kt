package cash.atto.wallet.model

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class VotersResponse(
    val apy: String,
    val voters: List<Voter>,
    val entities: List<VoterEntity>
)

@Serializable
data class Voter(
    val address: String,
    val label: String,
    val entity: String,
    val payToAddress: String? = null,
    val sharePercentage: Int,
    val addedAt: String,
    val description: String,
    val voteWeight: String,
    val lastVotedAt: Instant
) {

    val voteWeightPercentage
        get() = voteWeight.let {
            val weight = BigDecimal.parseString(voteWeight)
            val maxSupply = BigDecimal.parseString(
                cash.atto.commons.AttoAmount.MAX.raw.toString()
            )
            val hundred = BigDecimal.parseString("100.00")

            val mode = DecimalMode(
                decimalPrecision = 2,
                scale = 2,
                roundingMode = RoundingMode.ROUND_HALF_CEILING
            )

            val percentage = (weight * hundred).divide(maxSupply, decimalMode = mode)

            return@let percentage
        }

}

@Serializable
data class VoterEntity(
    val entity: String,
    val organization: String,
    val label: String,
    val website: String,
    val tags: List<String>,
    val addedAt: String,
    val description: String
)

fun Voter.calculateEntityWeightPercentage(allVoters: List<Voter>): BigDecimal {
    val entityVoters = allVoters.filter { it.entity == this.entity }

    val totalWeight = entityVoters.fold(BigDecimal.ZERO) { acc, voter ->
        acc + BigDecimal.parseString(voter.voteWeight)
    }

    val maxSupply = BigDecimal.parseString(
        cash.atto.commons.AttoAmount.MAX.raw.toString()
    )
    val hundred = BigDecimal.parseString("100.00")

    val mode = DecimalMode(
        decimalPrecision = 2,
        scale = 2,
        roundingMode = RoundingMode.ROUND_HALF_CEILING
    )

    return (totalWeight * hundred).divide(maxSupply, decimalMode = mode)
}
