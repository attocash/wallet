package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoPageFrame
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.components.common.AttoTransactionCard
import cash.atto.wallet.components.common.AttoTransactionDetailsDialog
import cash.atto.wallet.platform.exportCsvFile
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_text_muted
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.isCompactWidth
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.uistate.transactions.TransactionsSummaryUiState
import cash.atto.wallet.viewmodel.TransactionsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun TransactionsScreen(onBackClick: () -> Unit) {
    val viewModel = koinViewModel<TransactionsViewModel>()
    val uiState = viewModel.state.collectAsState()

    TransactionsContent(
        loadedTransactions = uiState.value.loadedTransactions,
        fullHistorySummary = uiState.value.fullHistorySummary,
        isLoadingInitial = uiState.value.isLoadingInitial,
        isLoadingMore = uiState.value.isLoadingMore,
        hasMore = uiState.value.hasMore,
        onLoadMore = viewModel::loadMore,
        onBackClick = onBackClick,
        onExport = { selectedTypes, sink ->
            viewModel.exportTransactions(selectedTypes, sink)
        },
    )
}

@Composable
fun TransactionsContent(
    loadedTransactions: List<TransactionUiState>,
    fullHistorySummary: TransactionsSummaryUiState,
    isLoadingInitial: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    onBackClick: () -> Unit,
    onExport: suspend (Set<TransactionType>, kotlinx.io.Sink) -> Unit,
) {
    val screenState = rememberTransactionsScreenState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val filteredTransactions =
        remember(loadedTransactions, screenState.selectedTypes) {
            loadedTransactions.filter { it.type in screenState.selectedTypes }
        }

    LaunchedEffect(listState, hasMore, isLoadingMore, loadedTransactions.size) {
        snapshotFlow {
            shouldLoadMoreTransactions(
                hasMore = hasMore,
                isLoadingMore = isLoadingMore,
                loadedTransactionsCount = loadedTransactions.size,
                lastVisibleItemIndex =
                    listState.layoutInfo.visibleItemsInfo
                        .lastOrNull()
                        ?.index,
                totalItemsCount = listState.layoutInfo.totalItemsCount,
            )
        }.collect { shouldLoadMore ->
            if (shouldLoadMore) {
                onLoadMore()
            }
        }
    }

    AttoPageFrame(
        title = "Transaction History",
        subtitle = "Recent first. Older entries load on demand.",
        onBack = onBackClick,
        scrollable = false,
        actions = {
            TransactionsTopActions(
                onFilterClick = screenState::showFilterDialog,
                onExportClick = {
                    val fileName = "transactions-${Clock.System.now().toEpochMilliseconds()}.csv"
                    scope.launch {
                        val message =
                            runCatching {
                                val export =
                                    exportCsvFile(fileName) { sink ->
                                        onExport(screenState.selectedTypes, sink)
                                    }
                                "CSV exported to ${export.location}"
                            }.getOrElse { error ->
                                error.message ?: "Unable to export CSV."
                            }
                        screenState.showExportMessage(message)
                    }
                },
            )
        },
    ) {
        TransactionsDialogs(
            screenState = screenState,
        )

        TransactionsHistoryList(
            listState = listState,
            loadedTransactions = loadedTransactions,
            filteredTransactions = filteredTransactions,
            summary = fullHistorySummary,
            isLoadingInitial = isLoadingInitial,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            isFilterActive = screenState.isFilterActive,
            onLoadMore = onLoadMore,
            onTransactionClick = screenState::selectTransaction,
        )
    }
}

@Composable
private fun TransactionsTopActions(
    onFilterClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    AttoButton(
        text = "Filter",
        onClick = onFilterClick,
        icon = Icons.Outlined.FilterList,
        variant = AttoButtonVariant.Outlined,
    )
    AttoButton(
        text = "Export",
        onClick = onExportClick,
        icon = Icons.Outlined.Download,
        variant = AttoButtonVariant.Outlined,
    )
}

@Composable
private fun TransactionsDialogs(screenState: TransactionsScreenState) {
    screenState.selectedTransaction?.let { transaction ->
        AttoTransactionDetailsDialog(
            transaction = transaction,
            onDismiss = screenState::dismissTransaction,
        )
    }
    if (screenState.isFilterDialogVisible) {
        TransactionFilterDialog(
            selectedTypes = screenState.selectedTypes,
            onDismiss = screenState::dismissFilterDialog,
            onApply = screenState::applyFilter,
        )
    }
    screenState.exportMessage?.let { message ->
        TransactionsMessageDialog(
            title = "Export",
            message = message,
            onDismiss = screenState::clearExportMessage,
        )
    }
}

@Composable
private fun TransactionsHistoryList(
    listState: LazyListState,
    loadedTransactions: List<TransactionUiState>,
    filteredTransactions: List<TransactionUiState>,
    summary: TransactionsSummaryUiState,
    isLoadingInitial: Boolean,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    isFilterActive: Boolean,
    onLoadMore: () -> Unit,
    onTransactionClick: (TransactionUiState) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TransactionsSummaryGrid(
                summary = summary,
                displayedTransactions = filteredTransactions,
                isFilterActive = isFilterActive,
            )
        }

        when {
            isLoadingInitial && loadedTransactions.isEmpty() -> {
                item {
                    TransactionsLoadingCard(
                        message = "Loading recent transactions...",
                    )
                }
            }

            filteredTransactions.isEmpty() -> {
                item {
                    TransactionsInfoCard(
                        message =
                            if (loadedTransactions.isEmpty()) {
                                "No transactions yet. Once your wallet starts moving ATTO, the history will appear here."
                            } else if (hasMore) {
                                "No loaded transactions match the current filter yet. Load older entries to keep searching."
                            } else {
                                "No transactions match the current filter."
                            },
                    )
                }
            }

            else -> {
                items(
                    items = filteredTransactions,
                    key = { transaction -> transaction.hash ?: transaction.height.value.toString() },
                ) { transaction ->
                    AttoTransactionCard(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) },
                    )
                }
            }
        }

        if (isLoadingMore) {
            item {
                TransactionsLoadingCard(
                    message = "Loading older transactions...",
                )
            }
        } else if (!isLoadingInitial && hasMore) {
            item {
                TransactionsLoadMoreCard(
                    loadedTransactions = loadedTransactions,
                    filteredTransactions = filteredTransactions,
                    isFilterActive = isFilterActive,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun TransactionsLoadingCard(message: String) {
    AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun TransactionsInfoCard(message: String) {
    AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = message,
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun TransactionsLoadMoreCard(
    loadedTransactions: List<TransactionUiState>,
    filteredTransactions: List<TransactionUiState>,
    isFilterActive: Boolean,
    onLoadMore: () -> Unit,
) {
    AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text =
                    if (isFilterActive) {
                        "Showing ${filteredTransactions.size} matching transactions from ${loadedTransactions.size} loaded."
                    } else {
                        "Showing ${loadedTransactions.size} loaded transactions."
                    },
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            AttoButton(
                text = "Load Older Transactions",
                onClick = onLoadMore,
                variant = AttoButtonVariant.Outlined,
            )
        }
    }
}

@Composable
private fun TransactionsSummaryGrid(
    summary: TransactionsSummaryUiState,
    displayedTransactions: List<TransactionUiState>,
    isFilterActive: Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = isCompactWidth()
        val rows =
            listOf(
                Triple("Transactions", summary.totalTransactions.toString(), dark_text_primary),
                Triple("Total Received", summary.totalReceivedText, dark_success),
                Triple("Total Sent", summary.totalSentText, dark_text_primary),
                Triple("Net Change", summary.netChangeText, dark_success),
            )

        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (summary.isLoading) {
                    Text(
                        text = "Calculating full-history totals...",
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (isFilterActive) {
                    Text(
                        text = "Filter active: showing ${displayedTransactions.size} matching loaded transactions.",
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                rows.chunked(2).forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowItems.forEach { (label, value, accent) ->
                            TransactionsSummaryCard(
                                modifier = Modifier.weight(1f),
                                label = label,
                                value = value,
                                accent = accent,
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (summary.isLoading) {
                    Text(
                        text = "Calculating full-history totals...",
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                if (isFilterActive) {
                    Text(
                        text = "Filter active: showing ${displayedTransactions.size} matching loaded transactions.",
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    rows.forEach { (label, value, accent) ->
                        TransactionsSummaryCard(
                            modifier = Modifier.weight(1f),
                            label = label,
                            value = value,
                            accent = accent,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionFilterDialog(
    selectedTypes: Set<TransactionType>,
    onDismiss: () -> Unit,
    onApply: (Set<TransactionType>) -> Unit,
) {
    var localSelection by remember(selectedTypes) { mutableStateOf(selectedTypes) }

    Dialog(onDismissRequest = onDismiss) {
        AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Filter Transactions",
                color = dark_text_primary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionType.entries.forEach { type ->
                    val checked = type in localSelection
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(dark_bg)
                                .clickable {
                                    localSelection =
                                        if (checked) {
                                            localSelection - type
                                        } else {
                                            localSelection + type
                                        }
                                }.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                localSelection =
                                    if (isChecked) {
                                        localSelection + type
                                    } else {
                                        localSelection - type
                                    }
                            },
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = type.name,
                                color = dark_text_primary,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W600),
                            )
                            Text(
                                text = transactionTypeDescription(type),
                                color = dark_text_muted,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AttoButton(
                    text = "Reset",
                    onClick = { localSelection = TransactionType.entries.toSet() },
                    modifier = Modifier.weight(1f),
                )
                AttoButton(
                    text = "Apply",
                    onClick = {
                        onApply(
                            if (localSelection.isEmpty()) {
                                TransactionType.entries.toSet()
                            } else {
                                localSelection
                            },
                        )
                    },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TransactionsMessageDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                color = dark_text_primary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
            )
            Text(
                text = message,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
            AttoButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TransactionsSummaryCard(
    modifier: Modifier,
    label: String,
    value: String,
    accent: Color,
) {
    AttoPanelCard(modifier = modifier) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600),
        )
        Text(
            text = value,
            color = accent,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W700),
        )
    }
}

private fun transactionTypeDescription(type: TransactionType): String =
    when (type) {
        TransactionType.SEND -> "Outgoing transfers"
        TransactionType.RECEIVE -> "Incoming transfers"
        TransactionType.OPEN -> "Account opening entries"
        TransactionType.CHANGE -> "Representative change entries"
    }
