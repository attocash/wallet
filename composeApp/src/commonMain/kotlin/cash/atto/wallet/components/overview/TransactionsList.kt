package cash.atto.wallet.components.overview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_transactions_title
import cash.atto.wallet.ui.AttoWalletTheme
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
    Column(modifier) {
        Text(text = stringResource(Res.string.overview_transactions_title))

        LazyColumn {
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