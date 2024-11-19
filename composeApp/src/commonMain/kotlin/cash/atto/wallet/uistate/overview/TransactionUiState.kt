package cash.atto.wallet.uistate.overview

import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_arrow_down
import attowallet.composeapp.generated.resources.ic_arrow_up
import attowallet.composeapp.generated.resources.overview_hint_type_change
import attowallet.composeapp.generated.resources.overview_hint_type_from
import attowallet.composeapp.generated.resources.overview_hint_type_to
import attowallet.composeapp.generated.resources.overview_transaction_from
import attowallet.composeapp.generated.resources.overview_transaction_to
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.ui.secondaryGradient
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

data class TransactionUiState(
    val type: TransactionType,
    val amount: String?,
    val source: String
) {

    var shownAmount = amount?.let { a ->
        //check if first element is a sign
        if (a.firstOrNull() == '+' || a.firstOrNull() == '-') {
            val sign = a.split(' ').getOrNull(0)
            val number = a.split(' ').getOrNull(1)

            "$sign ${AttoFormatter.format(number)}"
        } else AttoFormatter.format(amount)
    } ?: AttoFormatter.format(amount)

    val icon: ImageVector
        @Composable
        get() = when (type) {
            TransactionType.SEND -> vectorResource(Res.drawable.ic_arrow_up)
            TransactionType.RECEIVE -> vectorResource(Res.drawable.ic_arrow_down)
            TransactionType.CHANGE -> Icons.Outlined.Refresh
        }

    val typeString
        @Composable
        get() = when (type) {
            TransactionType.SEND -> stringResource(Res.string.overview_hint_type_to)
            TransactionType.RECEIVE -> stringResource(Res.string.overview_hint_type_from)
            TransactionType.CHANGE -> stringResource(Res.string.overview_hint_type_change)
        }

    val shownSource
        @Composable
        get() = when (type) {
            TransactionType.SEND ->
                "${stringResource(Res.string.overview_transaction_to)} $source"

            TransactionType.RECEIVE ->
                "${stringResource(Res.string.overview_transaction_from)} $source"

            TransactionType.CHANGE ->
                "${stringResource(Res.string.overview_transaction_from)} $source"
        }

    val cardGradient: Brush
        @Composable
        get() {
            val colors = when (type) {
                TransactionType.SEND -> MaterialTheme.colors.primaryGradient
                else -> MaterialTheme.colors.secondaryGradient
            }

            return Brush.horizontalGradient(
                colors.map { it.copy(alpha = 0.2f) }
            )
        }

    val iconGradient: Brush
        @Composable
        get() {
            val colors = when (type) {
                TransactionType.SEND -> MaterialTheme.colors.primaryGradient
                else -> MaterialTheme.colors.secondaryGradient
            }

            return Brush.horizontalGradient(
                colors.map { it.copy(alpha = 0.7f) }
            )
        }
}

enum class TransactionType {
    SEND, RECEIVE, CHANGE;
}