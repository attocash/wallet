package cash.atto.wallet.screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_button
import attowallet.composeapp.generated.resources.send_from_address_hint
import attowallet.composeapp.generated.resources.send_from_amount_hint
import attowallet.composeapp.generated.resources.send_from_title
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal

@Composable
fun SendScreenDesktop() {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val sendNavState = remember {
        mutableStateOf(SendScreenState.SEND)
    }

    SendDesktop(
        uiState = uiState.value,
        navState = sendNavState.value,
        onAmountChanged = { amount ->
            viewModel.updateSendInfo(
                amount = amount,
                address = uiState.value.sendFromUiState.address
            )
        },
        onAddressChanged = { address ->
            viewModel.updateSendInfo(
                amount = uiState.value.sendFromUiState.amount,
                address = address
            )
        },
        onSendClicked = { sendNavState.value = SendScreenState.CONFIRM },
        onConfirmClicked = {
            coroutineScope.launch {
                viewModel.send()
                sendNavState.value = SendScreenState.RESULT
            }
        },
        onCancelClicked = { sendNavState.value = SendScreenState.SEND },
        onResultClosed = {
            coroutineScope.launch {
                viewModel.clearTransactionData()
                sendNavState.value = SendScreenState.SEND
            }
        }
    )
}

@Composable
fun SendDesktop(
    uiState: SendTransactionUiState,
    navState: SendScreenState,
    onAmountChanged: suspend (BigDecimal?) -> Unit,
    onAddressChanged: suspend (String?) -> Unit,
    onSendClicked: () -> Unit,
    onConfirmClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onResultClosed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.send_from_title),
            color = MaterialTheme.colors.primary,
            style = MaterialTheme.typography.h5
        )

        uiState.sendFromUiState
            .accountName
            ?.let { Text(text = it) }

        uiState.sendFromUiState
            .accountSeed
            ?.let {
                Text(
                    text = it,
                    textAlign = TextAlign.Center
                )
            }

        Text(
            text = "(${AttoFormatter.format(
                uiState.sendFromUiState.accountBalance
            )})"
        )

        TextField(
            value = uiState.sendFromUiState
                .amount
                ?.toString()
                .orEmpty(),
            onValueChange = {
                coroutineScope.launch {
                    onAmountChanged.invoke(it.toBigDecimalOrNull())
                }
            },
            placeholder = {
                Text(text = stringResource(Res.string.send_from_amount_hint))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        TextField(
            value = uiState.sendFromUiState
                .address
                .orEmpty(),
            onValueChange = {
                coroutineScope.launch {
                    onAddressChanged.invoke(it)
                }
            },
            placeholder = {
                Text(text = stringResource(Res.string.send_from_address_hint))
            }
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSendClicked,
            modifier = Modifier.width(120.dp)
        ) {
            Text(text = stringResource(Res.string.send_button))
        }
    }

    if (navState == SendScreenState.CONFIRM) {
        Dialog(onDismissRequest = onCancelClicked) {
            Card(Modifier.size(width = 400.dp, height = 500.dp)) {
                SendConfirmContent(
                    modifier = Modifier.padding(32.dp),
                    uiState = uiState.sendConfirmUiState,
                    onConfirm = onConfirmClicked,
                    onCancel = onCancelClicked
                )
            }
        }
    }

    if (navState == SendScreenState.RESULT) {
        Dialog(onDismissRequest = onResultClosed) {
            Card(Modifier.size(width = 400.dp, height = 500.dp)) {
                SendResult(
                    uiState = uiState.sendResultUiState,
                    onClose = onResultClosed
                )
            }
        }
    }
}

enum class SendScreenState {
    SEND, CONFIRM, RESULT;
}

@Preview
@Composable
fun SendDesktopPreview() {
    AttoWalletTheme {
        SendDesktop(
            uiState = SendTransactionUiState.DEFAULT,
            navState = SendScreenState.SEND,
            onAmountChanged = {},
            onAddressChanged = {},
            onSendClicked = {},
            onConfirmClicked = {},
            onCancelClicked = {},
            onResultClosed = {}
        )
    }
}