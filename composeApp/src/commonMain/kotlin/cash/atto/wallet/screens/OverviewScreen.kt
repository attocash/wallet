package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_receive
import attowallet.composeapp.generated.resources.overview_send
import cash.atto.wallet.components.overview.OverviewHeader
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.uistate.overview.TransactionListUiState
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OverviewScreen(
    onSettingsClicked: () -> Unit,
    viewModel: OverviewViewModel = viewModel { OverviewViewModel() }
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
            onSettingsClicked = onSettingsClicked,
            modifier = Modifier.fillMaxWidth()
        )

        TransactionsList(
            uiState = uiState.transactionListUiState,
            modifier = Modifier.weight(1f)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(Res.string.overview_receive))
            }

            Button(
                onClick = {},
                modifier = Modifier.weight(1f)
            ) {
                Text(text = stringResource(Res.string.overview_send))
            }
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