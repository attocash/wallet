package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import cash.atto.wallet.components.common.QrScannerView
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendScreenWeb() {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val sendNavState = remember {
        mutableStateOf(SendScreenState.SEND)
    }

    SendWeb(
        uiState = uiState.value,
        navState = sendNavState.value,
        onToggleInputMode = {
            coroutineScope.launch { viewModel.toggleInputMode() }
        },
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
fun SendWeb(
    uiState: SendTransactionUiState,
    navState: SendScreenState,
    onToggleInputMode: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onSendClicked: () -> Unit,
    onConfirmClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onResultClosed: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        if (navState == SendScreenState.SEND && uiState.sendFromUiState.showLoader)
            AttoLoader(alpha = 0.7f)

        SendFromWeb(
            uiState = uiState.sendFromUiState,
            onToggleInputMode = onToggleInputMode,
            onAmountChanged = onAmountChanged,
            onAddressChanged = onAddressChanged,
            onSendClicked = onSendClicked
        )

        val showResult = uiState.sendResultUiState.result != SendTransactionUiState.SendOperationResult.UNKNOWN

        if (navState == SendScreenState.CONFIRM || showResult) {
            Dialog(
                onDismissRequest = if (showResult) onResultClosed else onCancelClicked
            ) {
                Card(
                    modifier = Modifier.widthIn(max = 440.dp).wrapContentHeight(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    if (showResult) {
                        SendResultRedesigned(
                            uiState = uiState.sendResultUiState,
                            onClose = onResultClosed
                        )
                    } else {
                        SendConfirmContentRedesigned(
                            uiState = uiState.sendConfirmUiState,
                            onConfirm = onConfirmClicked,
                            onCancel = onCancelClicked,
                            isLoading = uiState.sendConfirmUiState.showLoader
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SendFromWeb(
    uiState: SendFromUiState,
    onToggleInputMode: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onSendClicked: () -> Unit
) {
    val (focusRequester) = FocusRequester.createRefs()
    var showQrScanner by remember { mutableStateOf(false) }
    var scannerError by remember { mutableStateOf<String?>(null) }

    if (showQrScanner) {
        Dialog(onDismissRequest = { showQrScanner = false }) {
            QrScannerView(
                modifier = Modifier.size(400.dp),
                onQrCodeScanned = { result ->
                    scannerError = null
                    onAddressChanged(result)
                    showQrScanner = false
                },
                onScanError = { message ->
                    scannerError = message
                    showQrScanner = false
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(28.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 40.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(Res.string.send_from_title),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = attoFontFamily()
        )

        Spacer(Modifier.height(24.dp))

        // Account info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                uiState.accountName?.let {
                    Text(
                        text = it,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = attoFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(4.dp))
                }

                uiState.accountSeed?.let { address ->
                    Text(
                        text = address,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(Modifier.height(12.dp))

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "${AttoFormatter.format(uiState.accountBalance)} ATTO",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = attoFontFamily()
                )

                Text(
                    text = uiState.accountBalanceUsd?.let { AttoFormatter.formatUsd(it) } ?: "USD price unavailable",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        // Amount field
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (uiState.isUsdMode) "Amount (USD)" else "Amount (ATTO)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Surface(
                    onClick = onToggleInputMode,
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (uiState.isUsdMode) "Switch to ATTO" else "Switch to USD",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            OutlinedTextField(
                value = uiState.amountString.orEmpty(),
                onValueChange = onAmountChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .onPreviewKeyEvent {
                        if (
                            it.key.keyCode == Key.Enter.keyCode ||
                            it.key.keyCode == Key.Tab.keyCode
                        ) {
                            focusRequester.requestFocus()
                            return@onPreviewKeyEvent true
                        }
                        return@onPreviewKeyEvent false
                    },
                placeholder = {
                    Text(text = stringResource(Res.string.send_from_amount_hint))
                },
                isError = uiState.showAmountError,
                supportingText = if (uiState.showAmountError) {
                    {
                        Text(
                            text = stringResource(Res.string.send_error_amount),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    {
                        Text(
                            text = uiState.equivalentDisplay,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusRequester.requestFocus() }
                ),
                singleLine = true
            )
        }

        Spacer(Modifier.height(12.dp))

        // Address field
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Recipient",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            OutlinedTextField(
                value = uiState.address.orEmpty(),
                onValueChange = {
                    scannerError = null
                    onAddressChanged.invoke(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onPreviewKeyEvent {
                        if (it.key.keyCode == Key.Enter.keyCode) {
                            onSendClicked.invoke()
                            return@onPreviewKeyEvent true
                        }
                        return@onPreviewKeyEvent false
                    },
                placeholder = {
                    Text(text = stringResource(Res.string.send_from_address_hint))
                },
                isError = uiState.showAddressError,
                supportingText = if (uiState.showAddressError) {
                    {
                        Text(
                            text = stringResource(Res.string.send_error_address),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                } else null,
                trailingIcon = {
                    IconButton(onClick = {
                        scannerError = null
                        showQrScanner = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR Code",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = { onSendClicked.invoke() }
                ),
                singleLine = true
            )

            scannerError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(28.dp))

        AttoButton(
            onClick = onSendClicked,
            modifier = Modifier.fillMaxWidth(0.5f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = stringResource(Res.string.send_button))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
