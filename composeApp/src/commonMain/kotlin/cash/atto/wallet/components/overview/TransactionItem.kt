package cash.atto.wallet.components.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TransactionItem(uiState: TransactionUiState) {
    Card(
        backgroundColor = MaterialTheme.colors.background,
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(uiState.icon, "operation type icon")

            Column(Modifier.weight(1f)) {
                Text(uiState.typeString)
                Text(uiState.amount)
            }

            Text(uiState.shownSource)
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