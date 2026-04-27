package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.wallet.model.getAddressLabel
import cash.atto.wallet.model.getStakingApy
import cash.atto.wallet.model.getVoter
import cash.atto.wallet.model.getVoterLabel
import cash.atto.wallet.model.mergeAccountEntries
import cash.atto.wallet.repository.AppStateRepository
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.repository.PendingReceivablesState
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.buildTransactionListUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val appStateRepository: AppStateRepository,
    private val walletManagerRepository: WalletManagerRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
    private val homeRepository: HomeRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountCollectorJob: Job? = null
    private var accountEntriesCollectorJob: Job? = null
    private var receivablesCollectorJob: Job? = null
    private var pendingReceivablesState: PendingReceivablesState = PendingReceivablesState.EMPTY
    private var currentRepresentativeAddress: String? = null
    private var recentEntries: List<AttoAccountEntry> = emptyList()
    private var currentBalance: BigDecimal? = null

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            _state.value = OverviewUiState.empty()

            appStateRepository.state.collect {
                val receiveAddress =
                    it
                        .getPublicKey()
                        ?.toAddress(AttoAlgorithm.V1)
                        ?.value

                if (receiveAddress != state.value.receiveAddress) {
                    clearAccountData()
                }

                _state.emit(
                    state.value.copy(receiveAddress = receiveAddress),
                )
            }
        }

        scope.launch {
            preferencesRepository.state.collect {
                emitUpdatedState()
            }
        }

        scope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    println(
                        "OverviewViewModel is collecting account information from wallet ${
                            AttoAddress(
                                AttoAlgorithm.V1,
                                wallet.publicKey,
                            )
                        }",
                    )

                    accountCollectorJob?.cancel()
                    accountCollectorJob =
                        scope.launch {
                            wallet.accountFlow.collect { account ->
                                println("Account $account")

                                currentRepresentativeAddress =
                                    AttoAddress(
                                        account.representativeAlgorithm,
                                        account.representativePublicKey,
                                    ).toString()

                                currentBalance =
                                    account.balance
                                        .toString(AttoUnit.ATTO)
                                        .toBigDecimal()

                                emitUpdatedState()
                            }
                        }

                    accountEntriesCollectorJob?.cancel()
                    accountEntriesCollectorJob =
                        scope.launch {
                            // Load persisted entries from DB so they show after a page refresh.
                            val persisted =
                                persistentAccountEntryRepository.listRecent(
                                    wallet.publicKey,
                                    RECENT_TRANSACTION_LIMIT,
                                )
                            if (persisted.isNotEmpty()) {
                                recentEntries = persisted
                                emitUpdatedState()
                            }

                            persistentAccountEntryRepository.flow(wallet.publicKey).collect { entry ->
                                recentEntries =
                                    mergeAccountEntries(
                                        current = recentEntries,
                                        incoming = entry,
                                        limit = RECENT_TRANSACTION_LIMIT,
                                    )
                                emitUpdatedState()
                            }
                        }
                }
        }

        receivablesCollectorJob?.cancel()
        receivablesCollectorJob =
            scope.launch {
                walletManagerRepository.pendingReceivablesState.collect {
                    pendingReceivablesState = it
                    _state.emit(
                        state.value.copy(
                            pendingReceivableCount = it.count,
                            pendingReceivableAmount = it.totalAmount,
                        ),
                    )
                }
            }
    }

    private fun calculateApy(): BigDecimal? {
        val homeResponse = homeRepository.homeResponse.value ?: return null
        val representativeAddress = currentRepresentativeAddress ?: return null
        val voter = homeResponse.getVoter(representativeAddress) ?: return null
        val globalApy =
            homeResponse.getStakingApy()?.let { BigDecimal.parseString(it) }
                ?: return null
        if (voter.sharePercentage == 0) return null
        val sharePercentage = BigDecimal.fromInt(voter.sharePercentage)
        val hundred = BigDecimal.fromInt(100)
        return (globalApy * sharePercentage).divide(hundred).roundToDigitPositionAfterDecimalPoint(
            digitPosition = 2,
            roundingMode = RoundingMode.ROUND_HALF_AWAY_FROM_ZERO,
        )
    }

    private suspend fun emitUpdatedState() {
        _state.emit(
            state.value.copy(
                balance = currentBalance,
                priceUsd = homeRepository.getPriceUsd(),
                apy = calculateApy(),
                transactionListUiState = buildRecentTransactionListUiState(),
                voterName =
                    currentRepresentativeAddress?.let {
                        homeRepository.homeResponse.value?.getVoterLabel(it)
                    },
            ),
        )
    }

    private suspend fun clearAccountData() {
        recentEntries = emptyList()
        currentBalance = null
        currentRepresentativeAddress = null
        _state.emit(
            state.value.copy(
                balance = null,
                transactionListUiState =
                    buildTransactionListUiState(
                        entries = emptyList(),
                    ),
                pendingReceivableCount = pendingReceivablesState.count,
                pendingReceivableAmount = pendingReceivablesState.totalAmount,
            ),
        )
    }

    private fun buildRecentTransactionListUiState() =
        buildTransactionListUiState(
            entries = recentEntries,
            addressLabelResolver = ::resolveAddressLabel,
            voterLabelResolver = ::resolveChangeLabel,
            hashLabelResolver = ::resolveHashLabel,
        )

    private fun resolveAddressLabel(address: String): String? =
        preferencesRepository.getAddressLabel(address)
            ?: homeRepository.homeResponse.value?.getAddressLabel(address)

    private fun resolveChangeLabel(address: String): String? =
        preferencesRepository.getAddressLabel(address)
            ?: homeRepository.homeResponse.value?.getVoterLabel(address)

    private fun resolveHashLabel(hash: String): String? = preferencesRepository.getHashLabel(hash)

    companion object {
        private const val RECENT_TRANSACTION_LIMIT = 64
    }
}
