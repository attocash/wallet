package cash.atto.wallet.uistate.settings

import cash.atto.wallet.model.Voter
import cash.atto.wallet.model.VoterEntity
import kotlin.time.Instant

data class VoterUIState(
    val currentVoter: String?,
    val currentVoterLabel: String? = null,
    val currentVoterApy: Double? = null,
    val currentVoterWeightPercentage: Double? = null,
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