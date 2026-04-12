package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_error_address
import attowallet.composeapp.generated.resources.send_error_amount
import attowallet.composeapp.generated.resources.send_scan_qr
import cash.atto.wallet.components.common.AttoAmountField
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoLoader
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoPageFrame
import cash.atto.wallet.components.common.AttoPanelCard
import cash.atto.wallet.components.common.AttoTransactionCard
import cash.atto.wallet.components.common.AttoTransactionDetailsDialog
import cash.atto.wallet.components.common.AttoTransactionSection
import cash.atto.wallet.components.common.attoAmountFieldColors
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_danger
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_text_dim
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.uistate.send.SendFromUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendScreen(
    onBackClick: () -> Unit,
    qrScannerContent: (@Composable (onResult: (String) -> Unit, onError: (String) -> Unit, onDismiss: () -> Unit) -> Unit)? = null,
) {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()
    val overviewViewModel = koinViewModel<OverviewViewModel>()
    val overviewUiState = overviewViewModel.state.collectAsState()
    val workCache = org.koin.compose.koinInject<cash.atto.wallet.repository.PersistentWorkCache>()
    val hasCachedWork = workCache.hasCachedWork.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val sendNavState = remember { mutableStateOf(SendScreenState.SEND) }

    SendScreenContent(
        uiState = uiState.value,
        recentTransactions =
            overviewUiState.value.transactionListUiState.transactions
                .filterNotNull()
                .filter { it.type == TransactionType.SEND }
                .take(5),
        navState = sendNavState.value,
        hasCachedWork = hasCachedWork.value,
        onBackClick = onBackClick,
        qrScannerContent = qrScannerContent,
        onPaymentRequestScanned = { paymentRequest ->
            coroutineScope.launch { viewModel.applyPaymentRequest(paymentRequest) }
        },
        onToggleInputMode = { coroutineScope.launch { viewModel.toggleInputMode() } },
        onAmountChanged = { amount ->
            coroutineScope.launch {
                viewModel.updateSendInfo(
                    amount = amount,
                    address = uiState.value.sendFromUiState.address,
                )
            }
        },
        onAddressChanged = { address ->
            coroutineScope.launch {
                if (address != null && address.contains("?amount=")) {
                    viewModel.applyPaymentRequest(address)
                } else {
                    viewModel.updateSendInfo(
                        amount = uiState.value.sendFromUiState.amountString,
                        address = address,
                    )
                }
            }
        },
        onSendClicked = {
            coroutineScope.launch {
                if (viewModel.checkTransactionData()) {
                    sendNavState.value = SendScreenState.CONFIRM
                }
            }
        },
        onConfirmClicked = {
            coroutineScope.launch {
                viewModel.showLoader()
                val startTime =
                    kotlin.time.TimeSource.Monotonic
                        .markNow()
                viewModel.send()
                val elapsedMs = startTime.elapsedNow().inWholeMilliseconds
                viewModel.setElapsedMs(elapsedMs)
                viewModel.hideLoader()
            }
        },
        onCancelClicked = { sendNavState.value = SendScreenState.SEND },
        onResultClosed = {
            coroutineScope.launch {
                viewModel.clearTransactionData()
                sendNavState.value = SendScreenState.SEND
            }
        },
    )
}

@Composable
private fun SendScreenContent(
    uiState: SendTransactionUiState,
    recentTransactions: List<TransactionUiState>,
    navState: SendScreenState,
    hasCachedWork: Boolean,
    onBackClick: () -> Unit,
    qrScannerContent: (@Composable (onResult: (String) -> Unit, onError: (String) -> Unit, onDismiss: () -> Unit) -> Unit)?,
    onPaymentRequestScanned: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onSendClicked: () -> Unit,
    onConfirmClicked: () -> Unit,
    onCancelClicked: () -> Unit,
    onResultClosed: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,
    ) {
        if (navState == SendScreenState.SEND && uiState.sendFromUiState.showLoader) {
            AttoLoader(alpha = 0.7f)
        }

        SendFromContent(
            uiState = uiState.sendFromUiState,
            recentTransactions = recentTransactions,
            onBackClick = onBackClick,
            qrScannerContent = qrScannerContent,
            onPaymentRequestScanned = onPaymentRequestScanned,
            onToggleInputMode = onToggleInputMode,
            onAmountChanged = onAmountChanged,
            onAddressChanged = onAddressChanged,
            onSendClicked = onSendClicked,
        )

        val showResult = uiState.sendResultUiState.result != SendTransactionUiState.SendOperationResult.UNKNOWN
        if (showResult) {
            SendResult(
                uiState = uiState.sendResultUiState,
                onClose = onResultClosed,
            )
        }

        if (navState == SendScreenState.CONFIRM && !showResult) {
            SendConfirmContent(
                uiState = uiState.sendConfirmUiState,
                onConfirm = onConfirmClicked,
                onCancel = onCancelClicked,
                hasCachedWork = hasCachedWork,
                isSending = uiState.sendConfirmUiState.showLoader,
            )
        }
    }
}

@Composable
private fun SendFromContent(
    uiState: SendFromUiState,
    recentTransactions: List<TransactionUiState>,
    onBackClick: () -> Unit,
    qrScannerContent: (@Composable (onResult: (String) -> Unit, onError: (String) -> Unit, onDismiss: () -> Unit) -> Unit)?,
    onPaymentRequestScanned: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onSendClicked: () -> Unit,
) {
    var showQrScanner = remember { mutableStateOf(false) }
    var scannerError = remember { mutableStateOf<String?>(null) }
    var showFeeInfo = remember { mutableStateOf(false) }
    var selectedTransaction = remember { mutableStateOf<TransactionUiState?>(null) }

    if (showQrScanner.value && qrScannerContent != null) {
        Dialog(onDismissRequest = { showQrScanner.value = false }) {
            qrScannerContent(
                { result ->
                    scannerError.value = null
                    onPaymentRequestScanned(result)
                    showQrScanner.value = false
                },
                { message ->
                    scannerError.value = message
                    showQrScanner.value = false
                },
                { showQrScanner.value = false },
            )
        }
    }

    if (showFeeInfo.value) {
        AttoModal(
            title = "Why Atto is Free?",
            onDismiss = { showFeeInfo.value = false },
            showDivider = false,
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            contentSpacing = 0.dp,
        ) {
            Text(
                text =
                    "Atto transactions don't have fees because the network doesn't pay miners " +
                        "or validators per transaction. Instead of charging users, Atto uses " +
                        "a tiny proof of work only to prevent spam - so sending money stays free.",
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    AttoPageFrame(
        title = "Send Atto",
        subtitle = "Instant, feeless transfers to any Atto address",
        onBack = onBackClick,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = maxWidth < 1120.dp

            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    SendFormPanel(
                        modifier = Modifier.fillMaxWidth(),
                        uiState = uiState,
                        scannerError = scannerError.value,
                        hasQrScanner = qrScannerContent != null,
                        onToggleInputMode = onToggleInputMode,
                        onAmountChanged = onAmountChanged,
                        onAddressChanged = onAddressChanged,
                        onShowQr = {
                            scannerError.value = null
                            showQrScanner.value = true
                        },
                        onSendClicked = onSendClicked,
                        onFeeInfoClick = { showFeeInfo.value = true },
                    )

                    Text(
                        text = "Recent Sent",
                        color = dark_text_primary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
                    )

                    if (recentTransactions.isEmpty()) {
                        AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Sent transfers will appear here after your first outgoing transaction.",
                                color = dark_text_secondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    } else {
                        recentTransactions.forEach { transaction ->
                            AttoTransactionCard(
                                transaction = transaction,
                                onClick = { selectedTransaction.value = transaction },
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    SendFormPanel(
                        modifier = Modifier.width(500.dp),
                        uiState = uiState,
                        scannerError = scannerError.value,
                        hasQrScanner = qrScannerContent != null,
                        onToggleInputMode = onToggleInputMode,
                        onAmountChanged = onAmountChanged,
                        onAddressChanged = onAddressChanged,
                        onShowQr = {
                            scannerError.value = null
                            showQrScanner.value = true
                        },
                        onSendClicked = onSendClicked,
                        onFeeInfoClick = { showFeeInfo.value = true },
                    )
                    SendRecentPanel(
                        modifier = Modifier.weight(1f),
                        transactions = recentTransactions,
                        onTransactionClick = { selectedTransaction.value = it },
                    )
                }
            }
        }
    }

    selectedTransaction.value?.let { transaction: TransactionUiState ->
        AttoTransactionDetailsDialog(
            transaction = transaction,
            onDismiss = { selectedTransaction.value = null },
        )
    }
}

@Composable
private fun SendFormPanel(
    modifier: Modifier,
    uiState: SendFromUiState,
    scannerError: String?,
    hasQrScanner: Boolean,
    onToggleInputMode: () -> Unit,
    onAmountChanged: (String?) -> Unit,
    onAddressChanged: (String?) -> Unit,
    onShowQr: () -> Unit,
    onSendClicked: () -> Unit,
    onFeeInfoClick: () -> Unit,
) {
    val availableAtto = uiState.accountBalance?.replace(",", "")?.toDoubleOrNull() ?: 0.0
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AttoPanelCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Available",
                        color = dark_text_dim,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.W600),
                    )
                    Text(
                        text = "${AttoFormatter.format(uiState.accountBalance)} ATTO",
                        color = dark_text_primary,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
                    )
                }
                Text(
                    text = uiState.accountBalanceUsd?.let { "$${AttoFormatter.format(it.toString())}" } ?: "$0.00",
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W500),
                )
            }
        }

        AttoPanelCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Send To",
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.W600),
                )
                OutlinedTextField(
                    value = uiState.address.orEmpty(),
                    onValueChange = { onAddressChanged(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter Atto address") },
                    isError = uiState.showAddressError,
                    trailingIcon =
                        if (hasQrScanner) {
                            {
                                IconButton(onClick = onShowQr) {
                                    Icon(
                                        imageVector = Icons.Default.QrCodeScanner,
                                        contentDescription = stringResource(Res.string.send_scan_qr),
                                        tint = dark_accent,
                                    )
                                }
                            }
                        } else {
                            null
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors = attoAmountFieldColors(uiState.showAddressError),
                    shape = RoundedCornerShape(8.dp),
                    supportingText = {
                        val message =
                            when {
                                uiState.showAddressError -> stringResource(Res.string.send_error_address)
                                scannerError != null -> scannerError
                                else -> null
                            }
                        message?.let {
                            Text(
                                text = it,
                                color = if (uiState.showAddressError) dark_danger else dark_text_secondary,
                            )
                        }
                    },
                )
            }
        }

        AttoPanelCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AttoAmountField(
                    value = uiState.amountString.orEmpty(),
                    onValueChange = { onAmountChanged(it) },
                    isUsdMode = uiState.isUsdMode,
                    onToggleCurrency = onToggleInputMode,
                    priceUsd = uiState.priceUsd,
                    label = "Amount",
                    isError = uiState.showAmountError,
                    errorText = if (uiState.showAmountError) stringResource(Res.string.send_error_amount) else null,
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { onSendClicked() }),
                    largeFontSize = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(25, 50, 75, 100).forEach { percent ->
                        AttoButton(
                            text = if (percent == 100) "MAX" else "$percent%",
                            modifier = Modifier.weight(1f),
                            variant = AttoButtonVariant.Secondary,
                            onClick = {
                                if (uiState.isUsdMode) onToggleInputMode()
                                val nextAmount = availableAtto * percent / 100.0
                                onAmountChanged(nextAmount.toString())
                            },
                        )
                    }
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(dark_surface)
                    .border(1.dp, dark_border, RoundedCornerShape(12.dp))
                    .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Network Fee",
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "Free",
                    color = dark_success,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W700),
                )
            }
            IconButton(onClick = onFeeInfoClick) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = "Why is it free?",
                    tint = dark_text_secondary,
                )
            }
        }

        AttoButton(
            text = "Continue",
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.amountString.isNullOrBlank() && !uiState.address.isNullOrBlank(),
            onClick = onSendClicked,
        )
    }
}

@Composable
private fun SendRecentPanel(
    modifier: Modifier,
    transactions: List<TransactionUiState>,
    onTransactionClick: (TransactionUiState) -> Unit,
) {
    AttoTransactionSection(
        title = "Recent Sent",
        transactions = transactions,
        modifier = modifier,
        emptyMessage = "Sent transfers will appear here after your first outgoing transaction.",
        onTransactionClick = onTransactionClick,
    )
}
