package cash.atto.wallet.uistate.overview

import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_hint_amount_from
import attowallet.composeapp.generated.resources.overview_hint_amount_to
import attowallet.composeapp.generated.resources.overview_hint_destination
import attowallet.composeapp.generated.resources.overview_hint_source
import org.jetbrains.compose.resources.getString

data class TransactionListUiState(
    val transactions: List<TransactionUiState?>,
    val showHint: Boolean
) {
    companion object {
        val DEFAULT = TransactionListUiState(
            transactions = List<TransactionUiState?>(2) { null },
            showHint = false
        )

        suspend fun empty() = TransactionListUiState(
            transactions = listOf(
                TransactionUiState(
                    type = TransactionType.SEND,
                    amount = getString(Res.string.overview_hint_amount_to),
                    source = getString(Res.string.overview_hint_destination)
                ),
                TransactionUiState(
                    type = TransactionType.RECEIVE,
                    amount = getString(Res.string.overview_hint_amount_from),
                    source = getString(Res.string.overview_hint_source)
                ),
            ),
            showHint = true
        )
    }
}