package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.repository.WalletAccountState
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.uistate.overview.OverviewAccountUiState
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.TransactionListUiState
import cash.atto.wallet.uistate.overview.buildTransactionUiStates
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OverviewViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
    private val homeRepository: HomeRepository,
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OverviewUiState.DEFAULT)
    val state = _state.asStateFlow()

    private var currentRepresentativeAddress: String? = null
    private val recentActivity = MutableStateFlow(RecentActivity())
    private var currentBalance: BigDecimal? = null
    private var accounts: List<WalletAccountState> = emptyList()
    private var selectedAccountIndex: UInt = 0U

    init {
        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.accountsState.collect {
                accounts = it
                emitUpdatedState()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.selectedAccountIndexState.collect {
                selectedAccountIndex = it.value
                emitUpdatedState()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            preferencesRepository.state.collect {
                emitUpdatedState()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
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

        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.publicKeyState.collectLatest { publicKey ->
                recentActivity.value = RecentActivity()
                _state.update {
                    it.copy(
                        receiveAddress = publicKey?.toAddress(AttoAlgorithm.V1)?.value,
                        transactionListUiState = TransactionListUiState.DEFAULT,
                    )
                }
                if (publicKey == null) return@collectLatest

                coroutineScope {
                    // Subscribe before reading so a commit during the read is also included.
                    launch(start = CoroutineStart.UNDISPATCHED) {
                        persistentAccountEntryRepository.flow(publicKey).collect { entry ->
                            recentActivity.update {
                                it.copy(
                                    entries = mergeAccountEntries(it.entries, entry, RECENT_TRANSACTION_LIMIT),
                                )
                            }
                            emitUpdatedState()
                        }
                    }

                    val initialEntries =
                        try {
                            persistentAccountEntryRepository.listRecent(publicKey, RECENT_TRANSACTION_LIMIT)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            println("Failed to load recent activity: ${error.message}")
                            null
                        }
                    recentActivity.update {
                        if (initialEntries == null) {
                            it.copy(status = TransactionListUiState.Status.UNAVAILABLE)
                        } else {
                            it.copy(
                                entries = mergeAccountEntries(initialEntries, it.entries, RECENT_TRANSACTION_LIMIT),
                                status = TransactionListUiState.Status.READY,
                            )
                        }
                    }
                    emitUpdatedState()
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            homeRepository.homeResponse.collect {
                emitUpdatedState()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.pendingReceivablesState.collect { pending ->
                _state.update {
                    it.copy(
                        pendingReceivableCount = pending.count,
                        pendingReceivableAmount = pending.totalAmount,
                    )
                }
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

    private fun emitUpdatedState() {
        _state.update {
            it.copy(
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
            )
        }
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
        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.selectAccount(index)
        }
    }

    fun addAccount(name: String) {
        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.addAccount(name)
        }
    }

    fun setAccountActive(
        index: UInt,
        active: Boolean,
    ) {
        viewModelScope.launch(Dispatchers.Default) {
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
        viewModelScope.launch(Dispatchers.Default) {
            walletManagerRepository.nameAccount(
                index = index,
                name = name,
            )
        }
    }

    private fun buildRecentTransactionListUiState(): TransactionListUiState {
        val (entries, status) = recentActivity.value
        if (status == TransactionListUiState.Status.LOADING) return TransactionListUiState.DEFAULT

        return TransactionListUiState(
            transactions =
                buildTransactionUiStates(
                    entries = entries,
                    addressLabelResolver = ::resolveAddressLabel,
                    voterLabelResolver = ::resolveChangeLabel,
                    hashLabelResolver = ::resolveHashLabel,
                ),
            status = status,
        )
    }

    private fun resolveAddressLabel(address: String): String? =
        preferencesRepository.getAddressLabel(address)
            ?: homeRepository.homeResponse.value?.getAddressLabel(address)

    private fun resolveChangeLabel(address: String): String? =
        preferencesRepository.getAddressLabel(address)
            ?: homeRepository.homeResponse.value?.getVoterLabel(address)

    private fun resolveHashLabel(hash: String): String? = preferencesRepository.getHashLabel(hash)

    private data class RecentActivity(
        val entries: List<AttoAccountEntry> = emptyList(),
        val status: TransactionListUiState.Status = TransactionListUiState.Status.LOADING,
    )

    companion object {
        private const val RECENT_TRANSACTION_LIMIT = 64
    }
}
