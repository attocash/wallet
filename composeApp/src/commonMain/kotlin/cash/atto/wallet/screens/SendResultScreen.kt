package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.send_close
import attowallet.composeapp.generated.resources.send_failure_title
import cash.atto.commons.AttoHeight
import cash.atto.commons.AttoUnit
import cash.atto.commons.toAddress
import cash.atto.commons.toHex
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoButtonVariant
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoTransactionCard
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import cash.atto.wallet.uistate.send.SendResultUiState
import cash.atto.wallet.uistate.send.SendTransactionUiState
import cash.atto.wallet.viewmodel.SendTransactionViewModel
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun SendResultScreen(onClose: () -> Unit) {
    val viewModel = koinViewModel<SendTransactionViewModel>()
    val uiState = viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val compact = isCompactWidth()

    SendResult(
        uiState = uiState.value.sendResultUiState,
        onClose = {
            coroutineScope.launch {
                viewModel.clearTransactionData()
                onClose.invoke()
            }
        },
        compact = compact,
    )
}

@OptIn(ExperimentalTime::class)
@Composable
fun SendResult(
    uiState: SendResultUiState,
    onClose: () -> Unit,
    compact: Boolean = false,
) {
    val isSuccess = uiState.result == SendTransactionUiState.SendOperationResult.SUCCESS
    val accentColor =
        if (isSuccess) {
            dark_success
        } else {
            dark_danger
        }

    AttoModal(
        title = if (isSuccess) "Transaction Sent" else stringResource(Res.string.send_failure_title),
        onDismiss = onClose,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier =
                    Modifier
                        .size(68.dp)
                        .background(dark_surface, RoundedCornerShape(16.dp))
                        .border(1.dp, dark_border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (isSuccess) Icons.Outlined.Done else Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(29.dp),
                    tint = accentColor,
                )
            }

            Text(
                text = "${AttoFormatter.format(uiState.amount)} ATTO",
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = dark_success,
                    ),
                color = accentColor,
            )

            uiState.amountUsd?.let {
                Text(
                    text = "~ \$${AttoFormatter.format(it)} USD",
                    style = MaterialTheme.typography.bodyMedium,
                    color = dark_text_tertiary,
                )
            }
        }

        val block = uiState.sendBlock
        val confirmedAmount =
            uiState.amount?.toStringExpanded()
                ?: runCatching { block?.amount?.toString(AttoUnit.ATTO) }.getOrNull()

        if (isSuccess) {
            AttoTransactionCard(
                transaction =
                    TransactionUiState(
                        type = TransactionType.SEND,
                        amount = confirmedAmount,
                        source =
                            block
                                ?.receiverPublicKey
                                ?.toAddress(block.receiverAlgorithm)
                                ?.value
                                ?: uiState.address.orEmpty(),
                        sourceLabel = null,
                        timestamp =
                            block?.timestamp?.value
                                ?: Clock.System.now(),
                        height = block?.height ?: AttoHeight(0u),
                        hash = block?.hash?.value?.toHex(),
                    ),
            )
        }

        if (isSuccess && uiState.elapsedMs != null) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    text = "Confirmed in ${uiState.elapsedMs}ms",
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 10.sp,
                        ),
                    color = dark_text_tertiary,
                )
            }
        }

        AttoButton(
            text = stringResource(Res.string.send_close),
            onClick = onClose,
            modifier = Modifier.fillMaxWidth(),
            variant = AttoButtonVariant.Outlined,
        )
    }
}

@Preview
@Composable
fun SendResultPreview() {
    AttoWalletTheme {
        SendResult(
            uiState =
                SendResultUiState(
                    result = SendTransactionUiState.SendOperationResult.SUCCESS,
                    amount = BigDecimal.TEN,
                    amountUsd = null,
                    address = "atto://address",
                    elapsedMs = 342,
                ),
            onClose = {},
        )
    }
}
