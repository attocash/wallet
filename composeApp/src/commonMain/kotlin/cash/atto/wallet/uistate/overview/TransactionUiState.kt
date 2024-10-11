package cash.atto.wallet.uistate.overview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.runtime.Composable
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.overview_hint_type_from
import attowallet.composeapp.generated.resources.overview_hint_type_to
import attowallet.composeapp.generated.resources.overview_transaction_from
import attowallet.composeapp.generated.resources.overview_transaction_to
import org.jetbrains.compose.resources.stringResource

data class TransactionUiState(
    val type: TransactionType,
    val amount: String,
    val source: String
) {
    val icon get() = when (type) {
        TransactionType.SEND -> Icons.AutoMirrored.Filled.Send
        TransactionType.RECEIVE -> Icons.Outlined.Add
    }

    val typeString
        @Composable
        get() = when (type) {
            TransactionType.SEND -> stringResource(Res.string.overview_hint_type_to)
            TransactionType.RECEIVE -> stringResource(Res.string.overview_hint_type_from)
        }

    val shownSource
        @Composable
        get() = when (type) {
            TransactionType.SEND ->
                "${stringResource(Res.string.overview_transaction_to)} $source"

            TransactionType.RECEIVE ->
                "${stringResource(Res.string.overview_transaction_from)} $source"
        }
}

enum class TransactionType {
    SEND, RECEIVE;
}