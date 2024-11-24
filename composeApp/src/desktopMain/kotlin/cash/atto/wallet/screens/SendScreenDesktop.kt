package cash.atto.wallet.screens

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_button
import attowallet.composeapp.generated.resources.send_error_address
import attowallet.composeapp.generated.resources.send_error_amount
import attowallet.composeapp.generated.resources.send_from_address_hint
import attowallet.composeapp.generated.resources.send_from_amount_hint
import attowallet.composeapp.generated.resources.send_from_title
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.send.SendFromUiState
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
            coroutineScope.launch {
                viewModel.updateSendInfo(
                    amount = amount,
                    address = uiState.value.sendFromUiState.address
                )
            }
        },
        onAddressChanged = { address ->
            coroutineScope.launch {
                viewModel.updateSendInfo(
                    amount = uiState.value.sendFromUiState.amountString,
                    address = address
                )
            }
        },
        onSendClicked = {
            coroutineScope.launch {
                if (viewModel.checkTransactionData())
                    sendNavState.value = SendScreenState.CONFIRM
            }
        },
        onConfirmClicked = {
            coroutineScope.launch {
                viewModel.showLoader()
                viewModel.send()
                viewModel.hideLoader()

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
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onSendClicked: () -> Unit,
    onConfirmClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onResultClosed: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        color = Color.Transparent
    ) {
        if (navState == SendScreenState.SEND && uiState.sendFromUiState.showLoader)
            AttoLoader(alpha = 0.7f)

        SendFromDesktop(
            uiState = uiState.sendFromUiState,
            onAmountChanged = onAmountChanged,
            onAddressChanged = onAddressChanged,
            onSendClicked = onSendClicked
        )

        if (navState == SendScreenState.CONFIRM) {
            Dialog(onDismissRequest = onCancelClicked) {
                Card(Modifier.size(width = 400.dp, height = 500.dp)) {
                    if (uiState.sendConfirmUiState.showLoader)
                        AttoLoader(alpha = 0.7f)

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
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SendFromDesktop(
    uiState: SendFromUiState,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onSendClicked: () -> Unit
) {
    val (focusRequester) = FocusRequester.createRefs()

    Column(
        modifier = Modifier.clip(RoundedCornerShape(50.dp))
//            .background(color = MaterialTheme.colors.surface)
            .padding(vertical = 48.dp, horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.send_from_title),
            color = MaterialTheme.colors
                .onSurface
                .copy(alpha = 0.55f),
            style = MaterialTheme.typography.h6
        )

        uiState.accountName?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.h5
            )
        }

        uiState.accountSeed?.let { address ->
            val displayAddress = address.substring(0, address.length / 2) +
                "\n" +
                address.substring(address.length / 2, address.length)

            Text(
                text = displayAddress,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h5
            )
        }

        Text(
            text = "(${AttoFormatter.format(
                uiState.accountBalance
            )})",
            style = MaterialTheme.typography.h5
        )

        TextField(
            value = uiState.amountString.orEmpty(),
            onValueChange = onAmountChanged,
            modifier = Modifier.onPreviewKeyEvent {
                if (
                    it.key.nativeKeyCode == Key.Enter.nativeKeyCode ||
                    it.key.nativeKeyCode == Key.Tab.nativeKeyCode
                ){
                    focusRequester.requestFocus()

                    return@onPreviewKeyEvent true
                }

                return@onPreviewKeyEvent false
            },
            placeholder = {
                Text(text = stringResource(Res.string.send_from_amount_hint))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusRequester.requestFocus() }
            )
        )

        if (uiState.showAmountError) {
            Text(
                text = stringResource(Res.string.send_error_amount),
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption
            )
        }

        TextField(
            value = uiState.address.orEmpty(),
            onValueChange = {
                onAddressChanged.invoke(it)
            },
            modifier = Modifier.focusRequester(focusRequester)
                .onPreviewKeyEvent {
                    if (it.key.nativeKeyCode == Key.Enter.nativeKeyCode){
                        onSendClicked.invoke()

                        return@onPreviewKeyEvent true
                    }

                    return@onPreviewKeyEvent false
                },
            placeholder = {
                Text(text = stringResource(Res.string.send_from_address_hint))
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onSendClicked.invoke() }
            )
        )

        if (uiState.showAddressError) {
            Text(
                text = stringResource(Res.string.send_error_address),
                color = MaterialTheme.colors.error,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption
            )
        }

        Spacer(Modifier.weight(1f))

        AttoButton(
            onClick = onSendClicked,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(text = stringResource(Res.string.send_button))
        }
    }
}

enum class SendScreenState {
    SEND, CONFIRM, RESULT;
}

@Preview
@Composable
fun SendFromDesktopPreview() {
    AttoWalletTheme {
        SendFromDesktop(
            uiState = SendFromUiState.DEFAULT,
            onAmountChanged = {},
            onAddressChanged = {},
            onSendClicked = {}
        )
    }
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