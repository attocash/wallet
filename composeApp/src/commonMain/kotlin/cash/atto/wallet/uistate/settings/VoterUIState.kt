package cash.atto.wallet.uistate.settings

import cash.atto.wallet.model.Voter
import cash.atto.wallet.model.VoterEntity
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.time.Instant

data class VoterUIState(
    val currentVoter: String?,
    val currentVoterLabel: String? = null,
    val currentVoterApy: BigDecimal? = null,
    val currentVoterWeightPercentage: BigDecimal? = null,
    val currentVoterEntityWeightPercentage: BigDecimal? = null,
    val currentVoterLastVotedAt: Instant? = null,
    val globalApy: String? = null,
    val voters: List<Voter> = emptyList(),
    val entities: List<VoterEntity> = emptyList(),
    val isLoading: Boolean = false,
    val showError: Boolean = false
) {
    companion object {
        val DEFAULT = VoterUIState(null)
    }
}