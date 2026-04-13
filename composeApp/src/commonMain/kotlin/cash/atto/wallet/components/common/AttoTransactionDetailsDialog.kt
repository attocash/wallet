package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState

@Composable
fun AttoTransactionDetailsDialog(
    transaction: TransactionUiState,
    compact: Boolean = false,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val hash = transaction.hash
    val explorerUrl = hash?.let { "https://atto.cash/explorer/transactions/$it" }

    AttoModal(
        title = "Transaction Details",
        onDismiss = onDismiss,
    ) {
        AttoCopyField(
            label = "Transaction Hash",
            value = hash ?: "Unavailable",
        )
        AttoDetailField(
            label = "Type",
            value =
                transaction.type.name
                    .lowercase()
                    .replaceFirstChar { it.uppercase() },
        )
        AttoDetailField(
            label = "Amount",
            value = transaction.shownAmount.ifBlank { "Unavailable" },
        )
        AttoCopyField(
            label =
                when (transaction.type) {
                    TransactionType.OPEN -> "Voter"
                    TransactionType.SEND -> "Receiver"
                    TransactionType.RECEIVE -> "Sender"
                    TransactionType.CHANGE -> "Voter"
                },
            value = transaction.source,
        )
        AttoDetailField(
            label = "Height",
            value = "#${transaction.shownHeight}",
        )
        AttoDetailField(
            label = "Timestamp",
            value = transaction.formattedTimestamp,
        )

        if (explorerUrl != null) {
            AttoButton(
                text = "View in Explorer",
                onClick = { uriHandler.openUri(explorerUrl) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Outlined.ArrowForward,
            )
        }
    }
}
