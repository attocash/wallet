package cash.atto.wallet.uistate.overview

import androidx.compose.runtime.Composable
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_hint_amount_from
import attowallet.composeapp.generated.resources.overview_hint_amount_to
import attowallet.composeapp.generated.resources.overview_hint_destination
import attowallet.composeapp.generated.resources.overview_hint_source
import cash.atto.commons.AttoHeight
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.stringResource

data class TransactionListUiState(
    val transactions: List<TransactionUiState?>,
    val showHint: Boolean
) {
    companion object {
        val DEFAULT = TransactionListUiState(
            transactions = List<TransactionUiState?>(2) { null },
            showHint = false
        )

        @Composable
        fun Empty() = listOf(
            TransactionUiState(
                type = TransactionType.SEND,
                amount = stringResource(Res.string.overview_hint_amount_to),
                source = stringResource(Res.string.overview_hint_destination),
                timestamp = Clock.System.now(),
                height = AttoHeight(1UL),
            ),
            TransactionUiState(
                type = TransactionType.RECEIVE,
                amount = stringResource(Res.string.overview_hint_amount_from),
                source = stringResource(Res.string.overview_hint_source),
                timestamp = Clock.System.now(),
                height = AttoHeight(0UL),
            ),
        )
    }
}