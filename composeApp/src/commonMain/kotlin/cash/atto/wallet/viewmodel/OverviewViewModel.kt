package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAlgorithm
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.wallet.model.defaultAccountName
import cash.atto.wallet.model.getAddressLabel
import cash.atto.wallet.model.getStakingApy
import cash.atto.wallet.model.getVoter
import cash.atto.wallet.model.getVoterLabel
import cash.atto.wallet.model.mergeAccountEntries
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.PendingReceivablesState
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.repository.WalletAccountState
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.overview.OverviewAccountUiState
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
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
    private val homeRepository: HomeRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var accountEntriesCollectorJob: Job? = null
    private var receivablesCollectorJob: Job? = null
    private var pendingReceivablesState: PendingReceivablesState = PendingReceivablesState.EMPTY
    private var currentRepresentativeAddress: String? = null
    private var recentEntries: List<AttoAccountEntry> = emptyList()
    private var currentBalance: BigDecimal? = null
    private var accounts: List<WalletAccountState> = emptyList()
    private var selectedAccountIndex: UInt = 0U

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        scope.launch {
            _state.value = OverviewUiState.empty()
        }

        scope.launch {
            walletManagerRepository.accountsState.collect {
                accounts = it
                emitUpdatedState()
            }
        }

        scope.launch {
            walletManagerRepository.selectedAccountIndexState.collect {
                selectedAccountIndex = it.value
                emitUpdatedState()
            }
        }

        scope.launch {
            preferencesRepository.state.collect {
                emitUpdatedState()
            }
        }

        scope.launch {
            walletManagerRepository.accountState.collect { account ->
                if (account == null) {
                    currentRepresentativeAddress = null
                    currentBalance = null
                    emitUpdatedState()
                    return@collect
                }

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

        scope.launch {
            walletManagerRepository.publicKeyState.collect { publicKey ->
                accountEntriesCollectorJob?.cancel()
                recentEntries = emptyList()

                if (publicKey == null) {
                    currentRepresentativeAddress = null
                    currentBalance = null
                    clearAccountData(receiveAddress = null)
                    return@collect
                }

                val receiveAddress =
                    publicKey
                        .toAddress(AttoAlgorithm.V1)
                        .value

                clearAccountData(receiveAddress = receiveAddress)

                accountEntriesCollectorJob =
                    scope.launch {
                        // Load persisted entries from DB so they show after a page refresh.
                        val persisted =
                            persistentAccountEntryRepository.listRecent(
                                publicKey,
                                RECENT_TRANSACTION_LIMIT,
                            )
                        if (persisted.isNotEmpty()) {
                            recentEntries = persisted
                            emitUpdatedState()
                        }

                        persistentAccountEntryRepository.flow(publicKey).collect { entry ->
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
                accounts = buildAccountUiStates(),
                selectedAccountIndex = selectedAccountIndex,
                transactionListUiState = buildRecentTransactionListUiState(),
                voterName =
                    currentRepresentativeAddress?.let {
                        homeRepository.homeResponse.value?.getVoterLabel(it)
                    },
            ),
        )
    }

    private suspend fun clearAccountData(receiveAddress: String? = state.value.receiveAddress) {
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
                receiveAddress = receiveAddress,
                accounts = buildAccountUiStates(),
                selectedAccountIndex = selectedAccountIndex,
            ),
        )
    }

    private fun buildAccountUiStates(): List<OverviewAccountUiState> =
        accounts.map { account ->
            val address = account.address.toString()
            val index = account.index.value
            OverviewAccountUiState(
                index = index,
                name = preferencesRepository.getAddressLabel(address) ?: defaultAccountName(index),
                address = address,
                balance =
                    account.account
                        ?.balance
                        ?.toString(AttoUnit.ATTO)
                        ?.toBigDecimal(),
                active = account.isActive,
            )
        }

    fun selectAccount(index: UInt) {
        scope.launch {
            walletManagerRepository.selectAccount(index)
        }
    }

    fun addAccount(name: String) {
        scope.launch {
            walletManagerRepository.addAccount(name)
        }
    }

    fun setAccountActive(
        index: UInt,
        active: Boolean,
    ) {
        scope.launch {
            walletManagerRepository.setAccountActive(
                index = index,
                active = active,
            )
        }
    }

    fun nameAccount(
        index: UInt,
        name: String,
    ) {
        scope.launch {
            walletManagerRepository.nameAccount(
                index = index,
                name = name,
            )
        }
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
