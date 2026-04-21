package cash.atto.wallet.viewmodel

import androidx.lifecycle.ViewModel
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoUnit
import cash.atto.wallet.model.TransactionHistorySummary
import cash.atto.wallet.model.TransactionsHistoryState
import cash.atto.wallet.model.getAddressLabel
import cash.atto.wallet.model.getVoterLabel
import cash.atto.wallet.repository.HomeRepository
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.repository.WalletManagerRepository
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.uistate.overview.buildTransactionListUiState
import cash.atto.wallet.uistate.transactions.TransactionsSummaryUiState
import cash.atto.wallet.uistate.transactions.TransactionsUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal.Companion.parseString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.io.Sink

class TransactionsViewModel(
    private val walletManagerRepository: WalletManagerRepository,
    private val persistentAccountEntryRepository: PersistentAccountEntryRepository,
    private val homeRepository: HomeRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(TransactionsUiState())
    val state = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default)
    private var accountEntriesCollectorJob: Job? = null
    private var summaryCollectorJob: Job? = null
    private var currentPublicKey: AttoPublicKey? = null
    private var historyState = TransactionsHistoryState()

    init {
        scope.launch {
            walletManagerRepository.state
                .filterNotNull()
                .collect { wallet ->
                    currentPublicKey = wallet.publicKey
                    historyState = TransactionsHistoryState()
                    _state.emit(
                        TransactionsUiState(
                            fullHistorySummary = buildSummaryUiState(),
                        ),
                    )

                    loadInitialPage(wallet.publicKey)

                    summaryCollectorJob?.cancel()
                    summaryCollectorJob =
                        scope.launch {
                            loadSummary(wallet.publicKey)
                        }

                    accountEntriesCollectorJob?.cancel()
                    accountEntriesCollectorJob =
                        scope.launch {
                            persistentAccountEntryRepository.flow(wallet.publicKey).collect { entry ->
                                if (entry.publicKey != currentPublicKey) return@collect

                                historyState = historyState.withLiveEntry(entry)

                                emitState(
                                    isLoadingInitial = false,
                                    isLoadingMore = false,
                                    hasMore = historyState.hasMore,
                                )
                            }
                        }
                }
        }
    }

    fun loadMore() {
        val publicKey = currentPublicKey ?: return
        val beforeHeightExclusive = historyState.nextBeforeHeightExclusive ?: return
        val currentState = state.value
        if (currentState.isLoadingInitial || currentState.isLoadingMore || !currentState.hasMore) {
            return
        }

        scope.launch {
            _state.emit(currentState.copy(isLoadingMore = true))

            val page =
                persistentAccountEntryRepository.listBefore(
                    publicKey = publicKey,
                    beforeHeightExclusive = beforeHeightExclusive,
                    limit = PAGE_SIZE,
                )

            if (publicKey != currentPublicKey) {
                return@launch
            }

            historyState = historyState.withOlderPage(page, PAGE_SIZE)

            emitState(
                isLoadingInitial = false,
                isLoadingMore = false,
                hasMore = historyState.hasMore,
            )
        }
    }

    suspend fun exportTransactions(
        selectedTypes: Set<TransactionType>,
        sink: Sink,
    ) {
        val publicKey = currentPublicKey ?: return
        persistentAccountEntryRepository.exportCsv(
            publicKey = publicKey,
            selectedTypes = selectedTypes,
            addressLabelResolver = { address ->
                homeRepository.homeResponse.value?.getAddressLabel(address)
            },
            voterLabelResolver = { address ->
                homeRepository.homeResponse.value?.getVoterLabel(address)
            },
            sink = sink,
        )
    }

    private suspend fun loadInitialPage(publicKey: AttoPublicKey) {
        val page =
            persistentAccountEntryRepository.listRecent(
                publicKey = publicKey,
                limit = PAGE_SIZE,
            )

        if (publicKey != currentPublicKey) {
            return
        }

        historyState = historyState.withInitialPage(page, PAGE_SIZE)

        emitState(
            isLoadingInitial = false,
            isLoadingMore = false,
            hasMore = historyState.hasMore,
        )
    }

    private suspend fun loadSummary(publicKey: AttoPublicKey) {
        val summary = persistentAccountEntryRepository.summary(publicKey)

        if (publicKey != currentPublicKey) {
            return
        }

        historyState = historyState.withSummary(summary)
        emitState(
            isLoadingInitial = state.value.isLoadingInitial,
            isLoadingMore = state.value.isLoadingMore,
            hasMore = historyState.hasMore,
        )
    }

    private suspend fun emitState(
        isLoadingInitial: Boolean,
        isLoadingMore: Boolean,
        hasMore: Boolean,
    ) {
        _state.emit(
            TransactionsUiState(
                loadedTransactions = buildLoadedTransactions(),
                isLoadingInitial = isLoadingInitial,
                isLoadingMore = isLoadingMore,
                hasMore = hasMore,
                fullHistorySummary = buildSummaryUiState(),
            ),
        )
    }

    private fun buildLoadedTransactions(): List<TransactionUiState> =
        buildTransactionListUiState(
            entries = historyState.loadedEntries,
            addressLabelResolver = { address ->
                homeRepository.homeResponse.value?.getAddressLabel(address)
            },
            voterLabelResolver = { address ->
                homeRepository.homeResponse.value?.getVoterLabel(address)
            },
        ).transactions
            .filterNotNull()

    private fun buildSummaryUiState() =
        historyState.fullHistorySummary.toUiState(
            isLoading = historyState.isSummaryLoading,
        )

    private fun TransactionHistorySummary.toUiState(isLoading: Boolean) =
        TransactionsSummaryUiState(
            totalTransactions = totalTransactions,
            totalReceivedText = "+${formatAmount(totalReceived)}",
            totalSentText = "-${formatAmount(totalSent)}",
            netChangeText = formatSignedAmount(totalReceived, totalSent),
            isLoading = isLoading,
        )

    private fun formatSignedAmount(
        received: AttoAmount,
        sent: AttoAmount,
    ): String =
        if (received >= sent) {
            "+${formatAmount(received - sent)}"
        } else {
            "-${formatAmount(sent - received)}"
        }

    private fun formatAmount(amount: AttoAmount): String =
        AttoFormatter.format(
            parseString(
                amount.toString(AttoUnit.ATTO),
            ),
        )

    companion object {
        private const val PAGE_SIZE = 50
    }
}
