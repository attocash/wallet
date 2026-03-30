package cash.atto.wallet.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import cash.atto.commons.AttoHeight
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState

import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun TransactionItem(uiState: TransactionUiState) {
    var showDetailDialog by remember { mutableStateOf(false) }

    if (showDetailDialog && uiState.hash != null) {
        TransactionDetailDialog(
            uiState = uiState,
            onDismiss = { showDetailDialog = false }
        )
    }

    Box(
        Modifier.clip(MaterialTheme.shapes.medium)
            .background(brush = uiState.cardGradient)
            .clickable { showDetailDialog = true }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                Modifier.weight(0.57f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(52.dp, 52.dp)
                        .clip(CircleShape)
                        .rotate(45f)
                        .background(brush = uiState.iconGradient)
                ) {
                    Icon(
                        imageVector = uiState.icon,
                        contentDescription = "operation type icon",
                        modifier = Modifier.align(Alignment.Center)
                            .rotate(-45f),
                        tint = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(uiState.typeString)
                    Text(
                        text = uiState.shownAmount,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        text = uiState.formattedTimestamp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Column(
                modifier = Modifier.weight(0.43f),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "#" + uiState.shownHeight,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = uiState.source,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f),
                    textAlign = TextAlign.End,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun TransactionDetailDialog(
    uiState: TransactionUiState,
    onDismiss: () -> Unit
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val explorerUrl = "https://atto.cash/explorer/transactions/${uiState.hash}"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Transaction Details",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Type",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = uiState.typeString,
                    style = MaterialTheme.typography.bodyLarge
                )

                uiState.shownAmount.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Text(
                    text = "Height",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = "#${uiState.shownHeight}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = "Date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = uiState.formattedTimestamp,
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    text = if (uiState.type == TransactionType.CHANGE) "Voter" else "Address",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = uiState.source,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Transaction Hash",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text = uiState.hash.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(uiState.hash.orEmpty()))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Copy Hash")
                    }

                    TextButton(
                        onClick = { uriHandler.openUri(explorerUrl) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("View in Explorer")
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun TransactionItemPreview() {
    AttoWalletTheme {
        TransactionItem(
            TransactionUiState(
                type = TransactionType.SEND,
                amount = "A little Atto",
                source = "someone",
                timestamp = Clock.System.now(),
                height = AttoHeight(0UL)
            )
        )
    }
}