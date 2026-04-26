package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.QrCode2
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.common.*
import cash.atto.wallet.platform.setText
import cash.atto.wallet.platform.shareText
import cash.atto.wallet.ui.AttoPaymentRequests
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.isCompactWidth
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import cash.atto.wallet.viewmodel.ReceiveViewModel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReceiveScreen(onBackClick: () -> Unit) {
    val viewModel = koinViewModel<ReceiveViewModel>()
    val address = viewModel.address.collectAsState()
    val priceUsd = viewModel.priceUsd.collectAsState()
    val overviewViewModel = koinViewModel<OverviewViewModel>()
    val overviewState = overviewViewModel.state.collectAsState()
    ReceiveContent(
        address = address.value.orEmpty(),
        priceUsd = priceUsd.value,
        recentTransactions =
            overviewState.value.transactionListUiState.transactions
                .filterNotNull()
                .filter { it.type == TransactionType.RECEIVE }
                .take(5),
        onBackClick = onBackClick,
    )
}

@Composable
fun ReceiveContent(
    address: String,
    priceUsd: BigDecimal?,
    recentTransactions: List<TransactionUiState>,
    onBackClick: () -> Unit,
) {
    var requestedAmount by remember { mutableStateOf("") }
    var isUsdMode by remember { mutableStateOf(false) }
    var selectedTransaction by remember { mutableStateOf<TransactionUiState?>(null) }
    val compact = isCompactWidth()

    val amountAtto =
        if (isUsdMode) {
            val usd = requestedAmount.toDoubleOrNull()
            val price = priceUsd?.doubleValue(false)
            if (usd != null && price != null && price > 0) (usd / price).toString() else null
        } else {
            requestedAmount.ifBlank { null }
        }

    val paymentRequest =
        if (address.isNotBlank()) {
            AttoPaymentRequests.buildFromAtto(address, amountAtto)
        } else {
            ""
        }
    val walletDeepLink = AttoPaymentRequests.buildWalletDeepLinkFromPaymentRequest(paymentRequest).orEmpty()

    AttoPageFrame(
        title = "Receive Atto",
        subtitle = "Share your address or QR code to receive Atto",
        onBack = onBackClick,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val compact = isCompactWidth()

            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ReceiveQrColumn(
                        modifier = Modifier.fillMaxWidth(),
                        address = address,
                        paymentRequest = paymentRequest,
                        walletDeepLink = walletDeepLink,
                        requestedAmount = requestedAmount,
                        onRequestedAmountChange = { requestedAmount = it },
                        isUsdMode = isUsdMode,
                        onToggleCurrency = {
                            isUsdMode = !isUsdMode
                            requestedAmount = ""
                        },
                        priceUsd = priceUsd,
                    )
                    ReceiveActivityColumn(
                        modifier = Modifier.fillMaxWidth(),
                        transactions = recentTransactions,
                        onTransactionClick = { selectedTransaction = it },
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    ReceiveQrColumn(
                        modifier = Modifier.width(480.dp),
                        address = address,
                        paymentRequest = paymentRequest,
                        walletDeepLink = walletDeepLink,
                        requestedAmount = requestedAmount,
                        onRequestedAmountChange = { requestedAmount = it },
                        isUsdMode = isUsdMode,
                        onToggleCurrency = {
                            isUsdMode = !isUsdMode
                            requestedAmount = ""
                        },
                        priceUsd = priceUsd,
                    )
                    ReceiveActivityColumn(
                        modifier = Modifier.weight(1f),
                        transactions = recentTransactions,
                        onTransactionClick = { selectedTransaction = it },
                    )
                }
            }
        }
    }

    selectedTransaction?.let { transaction ->
        AttoTransactionDetailsDialog(
            transaction = transaction,
            onDismiss = { selectedTransaction = null },
        )
    }
}

@Composable
private fun ReceiveQrColumn(
    modifier: Modifier,
    address: String,
    paymentRequest: String,
    walletDeepLink: String,
    requestedAmount: String,
    onRequestedAmountChange: (String) -> Unit,
    isUsdMode: Boolean,
    onToggleCurrency: () -> Unit,
    priceUsd: BigDecimal?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
            AttoAmountField(
                value = requestedAmount,
                onValueChange = onRequestedAmountChange,
                isUsdMode = isUsdMode,
                onToggleCurrency = onToggleCurrency,
                priceUsd = priceUsd,
                label = "Request Amount (Optional)",
            )
        }

        AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .padding(24.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (paymentRequest.isNotBlank()) {
                    qrCodeImage(
                        modifier = Modifier.size(280.dp),
                        url = paymentRequest,
                        contentDescription = "Receive QR",
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.QrCode2,
                        contentDescription = "Receive QR",
                        tint = dark_text_secondary,
                        modifier = Modifier.size(120.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = address.ifBlank { "Waiting..." },
                    color = dark_text_primary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }

            if (requestedAmount.isNotBlank()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = "$requestedAmount ATTO",
                        color = dark_text_primary,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.W600),
                    )
                    Text(
                        text = "Requested amount",
                        color = dark_text_secondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else {
                Text(
                    text = "Scan to send Atto to your wallet",
                    color = dark_text_secondary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        val clipboard = LocalClipboard.current
        val coroutineScope = rememberCoroutineScope()
        var copiedWalletLink by remember { mutableStateOf(false) }
        var copiedAttoRequest by remember { mutableStateOf(false) }

        LaunchedEffect(copiedWalletLink) {
            if (copiedWalletLink) {
                delay(1000L)
                copiedWalletLink = false
            }
        }

        LaunchedEffect(copiedAttoRequest) {
            if (copiedAttoRequest) {
                delay(1000L)
                copiedAttoRequest = false
            }
        }

        val compactActions = isCompactWidth()
        val actionModifier = if (compactActions) Modifier.fillMaxWidth() else Modifier.weight(1f)

        val actionContent: @Composable () -> Unit = {
            AttoButton(
                onClick = {
                    if (walletDeepLink.isNotBlank()) {
                        coroutineScope.launch {
                            val shared = shareText(walletDeepLink)
                            if (!shared) {
                                clipboard.setText(walletDeepLink)
                            }
                            copiedWalletLink = true
                        }
                    }
                },
                variant = AttoButtonVariant.Outlined,
                text =
                    if (copiedWalletLink) {
                        ""
                    } else {
                        "Share URL"
                    },
                icon = if (copiedWalletLink) Icons.Outlined.Check else Icons.Outlined.Share,
                modifier = actionModifier,
                enabled = walletDeepLink.isNotBlank(),
            )

            AttoButton(
                onClick = {
                    if (paymentRequest.isNotBlank()) {
                        coroutineScope.launch {
                            clipboard.setText(paymentRequest)
                            copiedAttoRequest = true
                        }
                    }
                },
                variant = AttoButtonVariant.Outlined,
                text =
                    if (copiedAttoRequest) {
                        ""
                    } else {
                        "Copy Address"
                    },
                icon = if (copiedAttoRequest) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                modifier = actionModifier,
                enabled = paymentRequest.isNotBlank(),
            )
        }

        if (compactActions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                actionContent()
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                actionContent()
            }
        }
    }
}

@Composable
private fun ReceiveActivityColumn(
    modifier: Modifier,
    transactions: List<TransactionUiState>,
    onTransactionClick: (TransactionUiState) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        AttoTransactionSection(
            title = "Recent Received",
            transactions = transactions,
            modifier = Modifier.fillMaxWidth(),
            emptyMessage = "Incoming transfers will appear here after the wallet receives ATTO.",
            onTransactionClick = onTransactionClick,
        )
    }
}
