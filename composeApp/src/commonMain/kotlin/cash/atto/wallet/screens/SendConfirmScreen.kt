package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_confirm_sending
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendConfirmUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendConfirmScreen(
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit
) {
    KoinContext {
        val viewModel = koinViewModel<SendTransactionViewModel>()
        val uiState = viewModel.state.collectAsState()

        SendConfirm(
            uiState = uiState.value.sendConfirmUiState,
            onBackNavigation = onBackNavigation,
            onConfirm = onConfirm
        )
    }
}

@Composable
fun SendConfirm(
    uiState: SendConfirmUiState,
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(16.dp)
                    .padding(bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                            + 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.send_confirm_sending),
                    color = MaterialTheme.colors.primary,
                    style = MaterialTheme.typography.h5
                )

                Text(text = uiState.amount.toString())

                Button(onClick = onConfirm) {}
            }
        }
    )
}

@Preview
@Composable
fun SendConfirmPreview() {
    AttoWalletTheme {
        SendConfirm(
            uiState = SendConfirmUiState.DEFAULT,
            onBackNavigation = {},
            onConfirm = {}
        )
    }
}