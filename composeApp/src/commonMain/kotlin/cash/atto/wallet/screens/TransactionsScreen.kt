package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoPageFrame
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.components.common.AttoTransactionCard
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

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

    AttoPageFrame(
        title = "Transaction History",
        subtitle = "Complete record of all wallet transactions",
        onBack = onBackClick,
        scrollable = false,
        actions = {
            TransactionsActionButton("Filter", Icons.Outlined.FilterList)
            TransactionsActionButton("Export", Icons.Outlined.Download)
        },
    ) {
        selectedTransaction?.let { transaction ->
            TransactionDetailsDialog(
                transaction = transaction,
                onDismiss = { selectedTransaction = null },
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                TransactionsSummaryGrid(transactions = transactions)
            }

            if (transactions.isEmpty()) {
                item {
                    AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No transactions yet. Once your wallet starts moving ATTO, the full ledger will appear here.",
                            color = dark_text_secondary,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(
                    items = transactions,
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
        val compact = maxWidth < 760.dp
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
) {
    Row(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

@Composable
private fun TransactionDetailsDialog(
    transaction: TransactionUiState,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Transaction Details",
                color = dark_text_primary,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
            )
            TransactionsDetailRow(
                "Type",
                transaction.type.name
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
            )
            TransactionsDetailRow("Hash", transaction.hash ?: "Unavailable")
            TransactionsDetailRow("Source", transaction.source)
            TransactionsDetailRow("Amount", transaction.shownAmount)
            TransactionsDetailRow("Timestamp", transaction.formattedTimestamp)
            AttoButton(
                text = "Close",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TransactionsDetailRow(
    label: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600),
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(dark_bg)
                    .padding(12.dp),
        ) {
            Text(
                text = value,
                color = dark_text_primary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        HorizontalDivider(color = dark_border)
    }
}

private fun formatSignedAmount(value: Double): String =
    if (value >= 0) "+${formatAmount(value)}" else "-${formatAmount(kotlin.math.abs(value))}"

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
