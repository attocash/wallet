package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendResultUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal

@Composable
fun SendResultScreen() {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()

    SendResult(uiState.value.sendResultUiState)
}

@Composable
fun SendResult(
    uiState: SendResultUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.surface)
            .safeDrawingPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(64.dp))
    }
}

@Preview
@Composable
fun SendResultPreview() {
    AttoWalletTheme {
        SendResult(
            uiState = SendResultUiState(
                result = SendTransactionUiState.SendOperationResult.SUCCESS,
                amount = BigDecimal.TEN,
                address = "atto://address"
            )
        )
    }
}