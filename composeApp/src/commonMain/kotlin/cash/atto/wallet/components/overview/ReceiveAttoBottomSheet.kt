package cash.atto.wallet.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_copy
import attowallet.composeapp.generated.resources.ic_share
import attowallet.composeapp.generated.resources.overview_receive_address
import attowallet.composeapp.generated.resources.overview_receive_copy
import attowallet.composeapp.generated.resources.overview_receive_share
import attowallet.composeapp.generated.resources.send_from_amount_hint
import cash.atto.wallet.components.common.AttoAmountInputField
import cash.atto.wallet.components.common.BottomSheet
import cash.atto.wallet.components.common.QRCodeImage
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoPaymentRequests
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.gray_100
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ionspin.kotlin.bignum.decimal.RoundingMode
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveAttoBottomSheet(
    onDismissRequest: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    address: String?,
    priceUsd: BigDecimal?,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
) = address?.let {
    BottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        ReceiveAttoContent(
            address = address,
            priceUsd = priceUsd,
            onCopy = onCopy,
            onShare = onShare
        )
    }
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun ReceiveAttoContent(
    address: String,
    priceUsd: BigDecimal?,
    onPaymentRequestChanged: (String) -> Unit = {},
    onCopy: (String) -> Unit,
    onShare: ((String) -> Unit)? = null
) {
    var amountInput by rememberSaveable(address) { mutableStateOf("") }
    var isUsdMode by rememberSaveable(address) { mutableStateOf(false) }
    val amountAtto = receiveAmountAtto(amountInput, isUsdMode, priceUsd)
    val equivalentDisplay = receiveEquivalentDisplay(amountInput, isUsdMode, priceUsd)
    val paymentRequest = AttoPaymentRequests.buildFromAtto(address, amountAtto)

    LaunchedEffect(paymentRequest) {
        onPaymentRequestChanged(paymentRequest)
    }

    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        ReceiveAttoContentCompact(
            paymentRequest = paymentRequest,
            displayAddress = displayAddress(address),
            amountInput = amountInput,
            isUsdMode = isUsdMode,
            equivalentDisplay = equivalentDisplay,
            onAmountInputChanged = { amountInput = sanitizeAmountInput(it) },
            onToggleInputMode = {
                isUsdMode = !isUsdMode
                amountInput = ""
            },
            onCopy = onCopy,
            onShare = onShare
        )
    } else {
        ReceiveAttoContentExtended(
            paymentRequest = paymentRequest,
            displayAddress = displayAddress(address),
            amountInput = amountInput,
            isUsdMode = isUsdMode,
            equivalentDisplay = equivalentDisplay,
            onAmountInputChanged = { amountInput = sanitizeAmountInput(it) },
            onToggleInputMode = {
                isUsdMode = !isUsdMode
                amountInput = ""
            },
            onCopy = onCopy,
            onShare = onShare
        )
    }
}

@Composable
fun ReceiveAttoContentCompact(
    paymentRequest: String,
    displayAddress: String,
    amountInput: String,
    isUsdMode: Boolean,
    equivalentDisplay: String,
    onAmountInputChanged: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: ((String) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.overview_receive_address),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = displayAddress,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(Modifier.height(240.dp)) {
            Text(
                text = displayAddress,
                modifier = Modifier
                    .wrapContentSize(
                        align = Alignment.Center,
                        unbounded = true
                    )
                    .width(800.dp)
                    .padding(top = 40.dp),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                textAlign = TextAlign.Center,
                softWrap = false,
                style = MaterialTheme.typography.displayMedium
            )

            QRCodeImage(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(32.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .padding(18.dp),
                url = paymentRequest,
                contentDescription = "QR"
            )
        }

        ReceiveAmountField(
            amountInput = amountInput,
            isUsdMode = isUsdMode,
            equivalentDisplay = equivalentDisplay,
            onAmountInputChanged = onAmountInputChanged,
            onToggleInputMode = onToggleInputMode,
            modifier = Modifier.width(qrContentWidth)
        )

        ReceiveActionButtons(
            paymentRequest = paymentRequest,
            fillWidth = true,
            onCopy = onCopy,
            onShare = onShare
        )
    }
}

@Composable
fun ReceiveAttoContentExtended(
    paymentRequest: String,
    displayAddress: String,
    amountInput: String,
    isUsdMode: Boolean,
    equivalentDisplay: String,
    onAmountInputChanged: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: ((String) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 760.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.overview_receive_address),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = displayAddress,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        QRCodeImage(
            modifier = Modifier
                .size(qrContentWidth)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 3.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(18.dp),
            url = paymentRequest,
            contentDescription = "QR"
        )

        ReceiveAmountField(
            amountInput = amountInput,
            isUsdMode = isUsdMode,
            equivalentDisplay = equivalentDisplay,
            onAmountInputChanged = onAmountInputChanged,
            onToggleInputMode = onToggleInputMode,
            modifier = Modifier.width(qrContentWidth)
        )

        ReceiveActionButtons(
            paymentRequest = paymentRequest,
            fillWidth = true,
            onCopy = onCopy,
            onShare = onShare
        )
    }
}

@Composable
private fun ReceiveAmountField(
    amountInput: String,
    isUsdMode: Boolean,
    equivalentDisplay: String,
    onAmountInputChanged: (String) -> Unit,
    onToggleInputMode: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "REQUESTED AMOUNT",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
            style = MaterialTheme.typography.titleMedium
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(gray_100)
                .padding(16.dp)
        ) {
            AttoAmountInputField(
                value = amountInput,
                onValueChange = onAmountInputChanged,
                isUsdMode = isUsdMode,
                onToggleInputMode = onToggleInputMode,
                equivalentDisplay = equivalentDisplay,
                placeholder = stringResource(Res.string.send_from_amount_hint)
            )
        }
    }
}

@Composable
private fun ReceiveActionButtons(
    paymentRequest: String,
    fillWidth: Boolean,
    onCopy: (String) -> Unit,
    onShare: ((String) -> Unit)?,
) {
    Row(
        modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = { onCopy(paymentRequest) },
            modifier = if (fillWidth) Modifier.weight(1f) else Modifier,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            contentPadding = PaddingValues(19.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_copy),
                    contentDescription = "Copy icon"
                )

                Text(
                    text = stringResource(Res.string.overview_receive_copy),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (onShare != null) {
            Button(
                onClick = { onShare(paymentRequest) },
                modifier = if (fillWidth) Modifier.wrapContentSize() else Modifier,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    disabledElevation = 0.dp,
                    hoveredElevation = 0.dp,
                    focusedElevation = 0.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(19.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_share),
                        contentDescription = "Share icon"
                    )

                    Text(
                        text = stringResource(Res.string.overview_receive_share),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ReceiveAttoContentCompactPreview() {
    AttoWalletTheme {
        ReceiveAttoContentCompact(
            paymentRequest = "address?amount=123",
            displayAddress = "address\naddress",
            amountInput = "12.34",
            isUsdMode = false,
            equivalentDisplay = "~ $3.09 USD",
            onAmountInputChanged = {},
            onToggleInputMode = {},
            onCopy = {},
            onShare = {}
        )
    }
}

@Preview
@Composable
fun ReceiveAttoContentExtendedPreview() {
    AttoWalletTheme {
        ReceiveAttoContentExtended(
            paymentRequest = "address?amount=123",
            displayAddress = "address\naddress",
            amountInput = "12.34",
            isUsdMode = false,
            equivalentDisplay = "~ $3.09 USD",
            onAmountInputChanged = {},
            onToggleInputMode = {},
            onCopy = {},
            onShare = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun ReceiveAttoBottomSheetPreview() {
    AttoWalletTheme {
        ReceiveAttoBottomSheet(
            onDismissRequest = {},
            address = "address",
            priceUsd = BigDecimal.parseString("0.25"),
            onCopy = {},
            onShare = {}
        )
    }
}

private fun sanitizeAmountInput(value: String): String {
    val builder = StringBuilder()
    var hasDecimalSeparator = false

    value.forEach { character ->
        when {
            character.isDigit() -> builder.append(character)
            character == '.' && !hasDecimalSeparator -> {
                hasDecimalSeparator = true
                builder.append(character)
            }
        }
    }

    return builder.toString()
}

private fun parseDecimal(value: String): BigDecimal? =
    try {
        value.takeIf { it.isNotBlank() }?.toBigDecimal()
    } catch (_: Exception) {
        null
    }

private fun receiveAmountAtto(
    amountInput: String,
    isUsdMode: Boolean,
    priceUsd: BigDecimal?
): String? {
    val parsedAmount = parseDecimal(amountInput) ?: return null

    return if (isUsdMode) {
        if (priceUsd == null || priceUsd == BigDecimal.ZERO) {
            null
        } else {
            parsedAmount.divide(
                priceUsd,
                DecimalMode(decimalPrecision = 30, roundingMode = RoundingMode.ROUND_HALF_CEILING)
            ).toStringExpanded()
        }
    } else {
        parsedAmount.toStringExpanded()
    }
}

private fun receiveEquivalentDisplay(
    amountInput: String,
    isUsdMode: Boolean,
    priceUsd: BigDecimal?
): String {
    val parsedAmount = parseDecimal(amountInput) ?: return ""

    return if (isUsdMode) {
        receiveAmountAtto(amountInput, true, priceUsd)?.let { attoAmount ->
            "~ ${
                BigDecimal.parseString(attoAmount)
                    .roundToDigitPositionAfterDecimalPoint(6, RoundingMode.ROUND_HALF_CEILING)
                    .toStringExpanded()
            } ATTO"
        } ?: "USD price unavailable"
    } else {
        if (priceUsd == null) {
            "USD price unavailable"
        } else {
            AttoFormatter.formatUsd(parsedAmount.multiply(priceUsd))
        }
    }
}

private fun displayAddress(address: String): String =
    address.substring(0, address.length / 2) +
            "\n" +
            address.substring(address.length / 2, address.length)

private val qrContentWidth = 280.dp
