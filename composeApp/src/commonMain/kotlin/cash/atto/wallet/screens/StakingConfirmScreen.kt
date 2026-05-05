package cash.atto.wallet.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.commons.AttoHeight
import cash.atto.wallet.components.common.AttoAccentInlineLabel
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoCapsLabel
import cash.atto.wallet.components.common.AttoCopyField
import cash.atto.wallet.components.common.AttoDetailField
import cash.atto.wallet.components.common.AttoModal
import cash.atto.wallet.components.common.AttoTransactionCard
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.dark_accent_on
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.coroutines.delay
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock

@Composable
fun StakingConfirmContent(
    voterLabel: String,
    voterAddress: String,
    voterApy: String,
    voterUptime: String,
    accountHeight: ULong?,
    hasCachedWork: Boolean,
    isChangingVoter: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    val now = Clock.System.now()
    val previewHeight = accountHeight?.let { it + 1u } ?: 1uL
    val previewTransaction =
        TransactionUiState(
            type = TransactionType.CHANGE,
            amount = null,
            source = voterAddress,
            sourceLabel = voterLabel,
            timestamp = now,
            height = AttoHeight(previewHeight),
            hash = null,
        )

    AttoModal(
        title = "Change Voter",
        onDismiss = onCancel,
    ) {
        AttoCopyField(
            label = "TO",
            value = voterAddress,
            labelTrailingContent = {
                AttoAccentInlineLabel(text = voterLabel)
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AttoDetailField(
                label = "APY",
                value = voterApy,
                modifier = Modifier.weight(1f),
            )
            AttoDetailField(
                label = "LAST VOTED",
                value = voterUptime,
                modifier = Modifier.weight(1f),
            )
        }

        AttoCapsLabel("PREVIEW")

        AttoTransactionCard(
            transaction = previewTransaction,
        )

        HorizontalDivider(color = dark_border)

        when {
            isChangingVoter -> {
                var elapsedMs by remember { mutableStateOf(0L) }
                LaunchedEffect(Unit) {
                    val start =
                        kotlin.time.TimeSource.Monotonic
                            .markNow()
                    while (true) {
                        elapsedMs = start.elapsedNow().inWholeMilliseconds
                        delay(10)
                    }
                }

                AttoButton(
                    text = "${elapsedMs}ms",
                    onClick = {},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                )
            }

            !hasCachedWork -> {
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
            }

            else -> {
                AttoButton(
                    text = "Change Voter",
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview
@Composable
private fun StakingConfirmPreview() {
    AttoWalletTheme {
        StakingConfirmContent(
            voterLabel = "0conservative",
            voterAddress = "atto://atto_1conservativepreviewaddress",
            voterApy = "7.2%",
            voterUptime = "2h ago",
            accountHeight = 42u,
            hasCachedWork = true,
            isChangingVoter = false,
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
private fun StakingConfirmLoadingPreview() {
    AttoWalletTheme {
        StakingConfirmContent(
            voterLabel = "1prudent",
            voterAddress = "atto://atto_1prudentpreviewaddress",
            voterApy = "5.8%",
            voterUptime = "Just now",
            accountHeight = 42u,
            hasCachedWork = false,
            isChangingVoter = false,
            onConfirm = {},
            onCancel = {},
        )
    }
}

@Preview
@Composable
private fun StakingConfirmSendingPreview() {
    AttoWalletTheme {
        StakingConfirmContent(
            voterLabel = "2cautious",
            voterAddress = "atto://atto_1cautiouspreviewaddress",
            voterApy = "4.6%",
            voterUptime = "15m ago",
            accountHeight = 42u,
            hasCachedWork = true,
            isChangingVoter = true,
            onConfirm = {},
            onCancel = {},
        )
    }
}
