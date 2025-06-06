package cash.atto.wallet.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cash.atto.commons.AttoHeight
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TransactionItem(uiState: TransactionUiState) {
    Box(
        Modifier.clip(MaterialTheme.shapes.medium)
            .background(brush = uiState.cardGradient)
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