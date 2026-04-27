package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material.icons.outlined.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState

@Composable
fun AttoTransactionCard(
    transaction: TransactionUiState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val accent =
        when (transaction.type) {
            TransactionType.OPEN -> dark_accent
            TransactionType.SEND -> Color(0xFFEF4444)
            TransactionType.RECEIVE -> dark_success
            TransactionType.CHANGE -> dark_violet
        }
    val amountText = transaction.shownAmount.replace(" ", "")
    val directionLabel =
        when (transaction.type) {
            TransactionType.OPEN -> "FROM"
            TransactionType.SEND -> "TO"
            TransactionType.RECEIVE -> "FROM"
            TransactionType.CHANGE -> "TO"
        }
    val sourceText = transaction.sourceLabel ?: transaction.source
    val sourceColor = if (transaction.sourceLabel != null) dark_accent else dark_text_muted
    val sourceFont = if (transaction.sourceLabel != null) attoFontFamily() else FontFamily.Monospace
    val sourceWeight = if (transaction.sourceLabel != null) FontWeight.W600 else FontWeight.W400
    val typeLabel =
        when (transaction.type) {
            TransactionType.OPEN -> "Open"
            TransactionType.SEND -> "Sent"
            TransactionType.RECEIVE -> "Received"
            TransactionType.CHANGE -> "Change"
        }
    val hashLabel = transaction.transactionLabel?.takeIf { it.isNotBlank() }

    AttoCard(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier =
                    Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector =
                        when (transaction.type) {
                            TransactionType.OPEN -> Icons.Outlined.LockOpen
                            TransactionType.SEND -> Icons.Outlined.ArrowUpward
                            TransactionType.RECEIVE -> Icons.Outlined.ArrowDownward
                            TransactionType.CHANGE -> Icons.Outlined.SyncAlt
                        },
                    contentDescription = transaction.type.name,
                    tint = accent,
                    modifier = Modifier.size(18.dp),
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = typeLabel,
                        color = Color.White,
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 15.sp,
                            ),
                    )
                    Text(
                        text = "#${transaction.shownHeight}",
                        color = dark_text_dim,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                            ),
                    )
                    if (hashLabel != null) {
                        TransactionHashLabel(
                            text = hashLabel,
                            color = dark_violet,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = directionLabel,
                        color = dark_text_dim,
                        style =
                            MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.W500,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                            ),
                    )
                    Text(
                        text = sourceText,
                        color = sourceColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style =
                            MaterialTheme.typography.bodySmall.copy(
                                fontFamily = sourceFont,
                                fontWeight = sourceWeight,
                                fontSize = if (transaction.sourceLabel != null) 12.sp else 11.sp,
                            ),
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                transaction.amount?.let {
                    Text(
                        text = amountText,
                        color = if (transaction.type == TransactionType.RECEIVE) dark_success else Color.White,
                        style =
                            MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.W600,
                                fontSize = 16.sp,
                            ),
                    )
                }
                Text(
                    text = transaction.formattedTimestamp,
                    color = dark_text_tertiary,
                    textAlign = TextAlign.End,
                    style =
                        MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 11.sp,
                        ),
                )
            }
        }
    }
}

@Composable
private fun TransactionHashLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.W600,
                fontSize = 12.sp,
            ),
    )
}
