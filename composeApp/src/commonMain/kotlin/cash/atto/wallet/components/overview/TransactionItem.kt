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
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
                        tint = MaterialTheme.colors.secondary
                    )
                }

                Column {
                    Text(uiState.typeString)
                    Text(
                        text = uiState.shownAmount,
                        color = MaterialTheme.colors.secondary
                    )
                }
            }

            Box(Modifier.weight(0.43f)) {
                Text(
                    text = uiState.source,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.44f),
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
                timestamp = Clock.System.now()
            )
        )
    }
}