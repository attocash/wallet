package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.toAddress
import cash.atto.commons.wallet.AttoWalletManager
import cash.atto.wallet.model.Voter
import cash.atto.wallet.model.calculateEntityWeightPercentage
import cash.atto.wallet.repository.VotersRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.settings.VoterUIState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VoterViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val votersRepository: VotersRepository
) : ViewModel() {

    private val _state = MutableStateFlow(VoterUIState.DEFAULT)
    val state = _state.asStateFlow()

    private val viewModelScope = CoroutineScope(Dispatchers.Default)

    init {
        viewModelScope.launch {
            _state.emit(state.value.copy(isLoading = true))

            // Fetch voters data first
            votersRepository.fetchVoters()

            // Then collect wallet state
            walletManagerRepository.state
                .collect { updateRepresentative(it) }
        }
    }

    suspend fun setVoter(address: String): Boolean {
        if (checkAddress(address)) {
            walletManagerRepository.changeRepresentative(
                AttoAddress.parse(address)
            )

            updateRepresentative(walletManagerRepository.state.value)

            return true
        }

        return false
    }

    private suspend fun checkAddress(address: String): Boolean {
        val result = AttoAddress.isValid(address)

        _state.emit(
            state.value.copy(showError = !result)
        )

        return result
    }

    private suspend fun updateRepresentative(
        walletManager: AttoWalletManager?
    ) {
        if (walletManager?.account == null)
            return

        val representativeAddress = walletManager.account!!
            .representativePublicKey
            .toAddress(AttoAlgorithm.V1)
            .value

        val votersResponse = votersRepository.votersResponse.value
        val voterLabel = votersRepository.getVoterLabel(representativeAddress)
        val userApy = votersRepository.calculateUserApy(representativeAddress)

        val voters = votersResponse?.voters ?: emptyList()

        val votersSorted = voters.sortedWith(
            compareByDescending<Voter> { it.sharePercentage }
                .thenBy { it.voteWeight }
        )

        val currentVoter = voters.find { it.address == representativeAddress }
        val currentVoterWeightPercentage = currentVoter?.voteWeightPercentage
        val currentVoterEntityWeightPercentage = currentVoter?.calculateEntityWeightPercentage(voters)
        val currentVoterLastVotedAt = currentVoter?.lastVotedAt

        _state.emit(
            VoterUIState(
                currentVoter = representativeAddress,
                currentVoterLabel = voterLabel,
                currentVoterApy = userApy,
                currentVoterWeightPercentage = currentVoterWeightPercentage,
                currentVoterEntityWeightPercentage = currentVoterEntityWeightPercentage,
                currentVoterLastVotedAt = currentVoterLastVotedAt,
                globalApy = votersResponse?.apy,
                voters = votersSorted,
                entities = votersResponse?.entities ?: emptyList(),
                isLoading = false,
                showError = false
            )
        )
    }
}