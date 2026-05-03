package cash.atto.wallet.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_confirm
import cash.atto.commons.AttoHeight
import cash.atto.wallet.components.common.*
import cash.atto.wallet.model.LabeledPreferenceEntry
import cash.atto.wallet.repository.PreferencesRepository
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_on
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_text_primary
import cash.atto.wallet.ui.isCompactWidth
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.uistate.send.SendConfirmUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun SendConfirmScreen(
    onBackNavigation: () -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()
    val preferencesRepository = koinInject<PreferencesRepository>()
    val preferences by preferencesRepository.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val compact = isCompactWidth()

    Box(Modifier.fillMaxSize()) {
        if (uiState.value.sendConfirmUiState.showLoader) {
            AttoLoader(alpha = 0.4f, darkMode = true)
        }

        SendConfirmContent(
            uiState = uiState.value.sendConfirmUiState,
            savedAddresses = preferences.addresses,
            isSending = uiState.value.sendConfirmUiState.showLoader,
            compact = compact,
            onConfirm = {
                coroutineScope.launch {
                    viewModel.showLoader()
                    viewModel.send()
                    viewModel.hideLoader()
                    onConfirm.invoke()
                }
            },
            onCancel = onCancel,
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun SendConfirmContent(
    uiState: SendConfirmUiState,
    savedAddresses: List<LabeledPreferenceEntry> = emptyList(),
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    hasCachedWork: Boolean = true,
    isSending: Boolean = false,
    compact: Boolean = false,
) {
    val now = Clock.System.now()
    val previewHeight = uiState.accountHeight?.let { it + 1u } ?: 1uL
    val receiverAddress = uiState.address?.let { normalizeAttoUri(it) } ?: "Unavailable"
    val selectedAddressLabel =
        savedAddresses
            .firstOrNull { it.value == uiState.address?.trim() }
            ?.label

    val previewTransaction =
        TransactionUiState(
            type = TransactionType.SEND,
            amount = uiState.amount?.toStringExpanded(),
            source = receiverAddress,
            sourceLabel = selectedAddressLabel,
            timestamp = now,
            height = AttoHeight(previewHeight),
            hash = null,
        )

    AttoModal(
        title = "Confirm Transaction",
        onDismiss = onCancel,
    ) {
        AttoCopyField(
            label = "TO",
            value = receiverAddress,
            labelTrailingContent = {
                selectedAddressLabel?.let { label ->
                    AttoAccentInlineLabel(text = label)
                }
            },
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AttoCapsLabel("AMOUNT")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = uiState.amount?.let { "${AttoFormatter.format(it)} ATTO" } ?: "Unavailable",
                    color = dark_text_primary,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W700,
                            fontSize = 20.sp,
                        ),
                )
                uiState.amountUsd?.let {
                    Text(
                        text = "~ \$${AttoFormatter.format(it)} USD",
                        color = dark_accent,
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.W600,
                            ),
                    )
                }
            }
        }

        AttoCapsLabel("PREVIEW")

        AttoTransactionCard(
            transaction = previewTransaction,
        )

        HorizontalDivider(color = dark_border)

        if (isSending) {
            var elapsedMs by remember { mutableStateOf(0L) }
            LaunchedEffect(Unit) {
                val start =
                    kotlin.time.TimeSource.Monotonic
                        .markNow()
                while (true) {
                    elapsedMs = start.elapsedNow().inWholeMilliseconds
                    kotlinx.coroutines.delay(10)
                }
            }
            AttoButton(
                text = "${elapsedMs}ms",
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            )
        } else if (!hasCachedWork) {
            AttoButton(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = dark_accent_on,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            AttoButton(
                text = stringResource(Res.string.send_confirm),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun normalizeAttoUri(address: String): String =
    when {
        address.startsWith("atto://") -> address
        address.startsWith("atto_") -> "atto://$address"
        else -> address
    }

enum class SendScreenState {
    SEND,
    CONFIRM,
    RESULT,
}

@Preview
@Composable
fun SendConfirmPreview() {
    AttoWalletTheme {
        SendConfirmContent(
            uiState =
                SendConfirmUiState(
                    amount = BigDecimal.TEN,
                    amountUsd = null,
                    address = "atto://address",
                    showLoader = false,
                    accountHeight = 42u,
                ),
            onConfirm = {},
            onCancel = {},
        )
    }
}
