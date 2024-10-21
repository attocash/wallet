package cash.atto.wallet.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendConfirmScreen(
    onBackNavigation: () -> Unit
) {
    KoinContext {
        val viewModel = koinViewModel<SendTransactionViewModel>()
        val uiState = viewModel.state.collectAsState()

        SendConfirm(onBackNavigation = onBackNavigation)
    }
}

@Composable
fun SendConfirm(
    onBackNavigation: () -> Unit
) {}

@Preview
@Composable
fun SendConfirmPreview() {
    AttoWalletTheme {
        SendConfirm(
            onBackNavigation = {}
        )
    }
}