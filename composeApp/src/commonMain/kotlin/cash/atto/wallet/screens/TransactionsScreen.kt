package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cash.atto.wallet.components.common.*
import cash.atto.wallet.platform.exportCsvFile
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock

@Composable
fun TransactionsScreen(onBackClick: () -> Unit) {
    KoinContext {
        val overviewViewModel = koinViewModel<OverviewViewModel>()
        val overviewUiState = overviewViewModel.state.collectAsState()

        TransactionsContent(
            transactions =
                overviewUiState.value.transactionListUiState.transactions
                    .filterNotNull(),
            onBackClick = onBackClick,
        )
    }
}

@Composable
fun TransactionsContent(
    transactions: List<TransactionUiState>,
    onBackClick: () -> Unit,
) {
    var selectedTransaction by remember { mutableStateOf<TransactionUiState?>(null) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var selectedTypes by remember {
        mutableStateOf(TransactionType.entries.toSet())
    }
    var exportMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val compact = isCompactWidth()
    val filteredTransactions =
        remember(transactions, selectedTypes) {
            transactions.filter { it.type in selectedTypes }
        }

    AttoPageFrame(
        title = "Transaction History",
        subtitle = "Complete record of all wallet transactions",
        onBack = onBackClick,
        scrollable = false,
        actions = {
            TransactionsActionButton(
                text = "Filter",
                icon = Icons.Outlined.FilterList,
                onClick = { showFilterDialog = true },
            )
            TransactionsActionButton(
                text = "Export",
                icon = Icons.Outlined.Download,
                onClick = {
                    val fileName = "transactions-${Clock.System.now().toEpochMilliseconds()}.csv"
                    scope.launch {
                        exportMessage =
                            runCatching {
                                val export = exportCsvFile(fileName, filteredTransactions)
                                "CSV exported to ${export.location}"
                            }.getOrElse { error ->
                                error.message ?: "Unable to export CSV."
                            }
                    }
                },
            )
        },
    ) {
        selectedTransaction?.let { transaction ->
            AttoTransactionDetailsDialog(
                transaction = transaction,
                onDismiss = { selectedTransaction = null },
            )
        }
        if (showFilterDialog) {
            TransactionFilterDialog(
                selectedTypes = selectedTypes,
                onDismiss = { showFilterDialog = false },
                onApply = {
                    selectedTypes = it
                    showFilterDialog = false
                },
            )
        }
        exportMessage?.let { message ->
            TransactionsMessageDialog(
                title = "Export",
                message = message,
                onDismiss = { exportMessage = null },
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                TransactionsSummaryGrid(transactions = filteredTransactions)
            }

            if (filteredTransactions.isEmpty()) {
                item {
                    AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (transactions.isEmpty()) {
                                "No transactions yet. Once your wallet starts moving ATTO, the full ledger will appear here."
                            } else {
                                "No transactions match the current filter."
                            },
                            color = dark_text_secondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(
                    items = filteredTransactions,
                    key = { transaction -> transaction.hash ?: transaction.height.value.toString() },
                ) { transaction ->
                    Box(modifier = Modifier.clickable { selectedTransaction = transaction }) {
                        AttoTransactionCard(transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionsSummaryGrid(transactions: List<TransactionUiState>) {
    val (received, sent) =
        remember(transactions) {
            transactions.fold(0.0 to 0.0) { (receivedTotal, sentTotal), transaction ->
                when (transaction.type) {
                    TransactionType.RECEIVE -> (receivedTotal + parseAmount(transaction.amount)) to sentTotal
                    TransactionType.SEND -> receivedTotal to (sentTotal + parseAmount(transaction.amount))
                    else -> receivedTotal to sentTotal
                }
            }
        }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val compact = isCompactWidth()
        val rows =
            listOf(
                Triple("Total Transactions", transactions.size.toString(), dark_text_primary),
                Triple("Total Received", "+${formatAmount(received)}", dark_success),
                Triple("Total Sent", "-${formatAmount(sent)}", dark_text_primary),
                Triple("Net Change", formatSignedAmount(received - sent), dark_success),
            )

        if (compact) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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

@Composable
private fun TransactionsActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.material3.Icon(
            imageVector = icon,
            contentDescription = text,
            tint = dark_text_primary,
        )
        Text(
            text = text,
            color = dark_text_primary,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600),
        )
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
                    onClick = { onApply(localSelection.ifEmpty { TransactionType.entries.toSet() }) },
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

private fun formatSignedAmount(value: Double): String =
    if (value >= 0) "+${formatAmount(value)}" else "-${formatAmount(kotlin.math.abs(value))}"

private fun transactionTypeDescription(type: TransactionType): String =
    when (type) {
        TransactionType.SEND -> "Outgoing transfers"
        TransactionType.RECEIVE -> "Incoming transfers"
        TransactionType.OPEN -> "Account opening entries"
        TransactionType.CHANGE -> "Representative change entries"
    }

private fun parseAmount(amount: String?): Double {
    val raw =
        amount
            .orEmpty()
            .replace("+", "")
            .replace("-", "")
            .replace(",", "")
            .trim()
    return raw.toDoubleOrNull() ?: 0.0
}

private fun formatAmount(amount: Double): String = AttoFormatter.format(amount.toString())
