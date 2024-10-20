package cash.atto.wallet.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_hint
import attowallet.composeapp.generated.resources.overview_transactions_title
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoColors
import cash.atto.wallet.uistate.overview.TransactionListUiState
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TransactionsList(
    uiState: TransactionListUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(Res.string.overview_transactions_title),
            color = MaterialTheme.colors.onSurface
        )

        if (uiState.showHint) {
            Card(backgroundColor = MaterialTheme.colors.primary) {
                Box(
                    modifier = Modifier.padding(horizontal = 8.dp)
                        .background(color = MaterialTheme.colors.background)
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.overview_hint),
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(uiState.transactions) { transaction ->
                transaction?.let { TransactionItem(it) }
            }
        }
    }
}

@Preview
@Composable
fun TransactionsListPreview() {
    AttoWalletTheme {
        TransactionsList(
            TransactionListUiState(
                transactions = listOf(
                    TransactionUiState(
                        type = TransactionType.SEND,
                        amount = "A little Atto",
                        source = "someone"
                    ),
                    TransactionUiState(
                        type = TransactionType.RECEIVE,
                        amount = "A lot of Atto",
                        source = "someone"
                    ),
                ),
                showHint = true
            )
        )
    }
}