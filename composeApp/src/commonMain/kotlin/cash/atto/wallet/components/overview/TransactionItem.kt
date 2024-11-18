package cash.atto.wallet.components.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
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
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TransactionItem(uiState: TransactionUiState) {
    Box(
        Modifier.clip(MaterialTheme.shapes.medium)
            .background(brush = uiState.cardGradient)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.width(52.dp)
                .height(52.dp)
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

            Column(Modifier.weight(1f)) {
                Text(uiState.typeString)
                Text(
                    text = AttoFormatter.format(uiState.amount),
                    style = MaterialTheme.typography.body2
                )
            }

            Text(
                text = uiState.shownSource,
                modifier = Modifier.width(96.dp),
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.body2
            )
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
                source = "someone"
            )
        )
    }
}