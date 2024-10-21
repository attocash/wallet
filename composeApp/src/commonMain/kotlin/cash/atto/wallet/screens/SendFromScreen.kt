package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_button
import attowallet.composeapp.generated.resources.send_from_address_hint
import attowallet.composeapp.generated.resources.send_from_amount_hint
import attowallet.composeapp.generated.resources.send_from_title
import attowallet.composeapp.generated.resources.send_scan_qr
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendFromScreen(
    onBackNavigation: () -> Unit,
    onSendClicked: () -> Unit,
) {
    KoinContext {
        val viewModel = koinViewModel<SendTransactionViewModel>()
        val uiState = viewModel.state.collectAsState()

        SendFrom(
            uiState = uiState.value.sendFromUiState,
            onBackNavigation = onBackNavigation,
            onSendClicked = onSendClicked
        )
    }
}


@Composable
fun SendFrom(
    uiState: SendFromUiState,
    onBackNavigation: () -> Unit,
    onSendClicked: () -> Unit
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        backgroundColor = MaterialTheme.colors.surface,
        content = {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(bottom = WindowInsets.systemBars
                        .asPaddingValues()
                        .calculateBottomPadding()
                            + 16.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(Res.string.send_from_title))
                uiState.accountName?.let { Text(text = it) }
                uiState.accountSeed?.let { Text(text = it) }
                Text(text = "(${uiState.accountBalance})")

                TextField(
                    value = uiState.amount?.toString().orEmpty(),
                    onValueChange = {},
                    placeholder = { stringResource(Res.string.send_from_amount_hint) }
                )

                TextField(
                    value = uiState.address.orEmpty(),
                    onValueChange = {},
                    placeholder = { stringResource(Res.string.send_from_address_hint) }
                )

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = onSendClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.send_button))
                }

                AttoOutlinedButton(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.send_scan_qr))
                }
            }
        }
    )
}

@Preview
@Composable
fun SendFromPreview() {
    AttoWalletTheme {
        SendFrom(
            uiState = SendFromUiState.DEFAULT,
            onBackNavigation = {},
            onSendClicked = {}
        )
    }
}