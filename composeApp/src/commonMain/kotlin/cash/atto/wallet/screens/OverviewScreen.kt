package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.overview.OverviewHeader
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.TransactionListUiState
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OverviewScreen(
    onSettingsClicked: () -> Unit,
    viewModel: OverviewViewModel = OverviewViewModel()
) {
    val uiState = viewModel.state.collectAsState()
    Overview(uiState.value, onSettingsClicked)
}

@Composable
fun Overview(
    uiState: OverviewUiState,
    onSettingsClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp)
    ) {
        OverviewHeader(
            uiState = uiState.headerUiState,
            onSettingsClicked = onSettingsClicked
        )

        TransactionsList(
            uiState = uiState.transactionListUiState,
            modifier = Modifier.weight(1f)
        )

        Row {
            Button(onClick = {}) {}
            Button(onClick = {}) {}
        }
    }
}

@Preview
@Composable
fun OverviewPreview() {
    AttoWalletTheme {
        Overview(
            OverviewUiState(
                OverviewHeaderUiState.DEFAULT,
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
            ),
            onSettingsClicked = {}
        )
    }
}