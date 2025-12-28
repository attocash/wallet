package cash.atto.wallet.components.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_transactions
import attowallet.composeapp.generated.resources.overview_hint
import attowallet.composeapp.generated.resources.overview_transactions_title
import cash.atto.commons.AttoHeight
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.uistate.overview.TransactionListUiState
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun TransactionsList(
    uiState: TransactionListUiState,
    modifier: Modifier = Modifier,
    titleSize: TextUnit = 20.sp
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_transactions),
                contentDescription = "Transactions",
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                text = stringResource(Res.string.overview_transactions_title),
                modifier = Modifier.padding(start = 8.dp),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = titleSize,
                fontWeight = FontWeight.W400,
            )
        }

        if (uiState.showHint) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(color = MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 12.dp)
            ) {
                Box(Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .background(brush = Brush.horizontalGradient(
                        colors = MaterialTheme.colorScheme
                            .primaryGradient
                            .map { it.copy(alpha = 0.2f) }
                    ))
                    .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.overview_hint),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        val shownItems = if (uiState.showHint) {
            TransactionListUiState.Empty()
        } else uiState.transactions

        LazyColumn(
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(shownItems) { transaction ->
                transaction?.let { TransactionItem(it) }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun TransactionsListPreview() {
    AttoWalletTheme {
        TransactionsList(
            TransactionListUiState(
                transactions = listOf(
                    TransactionUiState(
                        type = TransactionType.SEND,
                        amount = "A little Atto",
                        source = "someone",
                        timestamp = Clock.System.now(),
                        height = AttoHeight(1UL),
                    ),
                    TransactionUiState(
                        type = TransactionType.RECEIVE,
                        amount = "A lot of Atto",
                        source = "someone",
                        timestamp = Clock.System.now(),
                        height = AttoHeight(0UL),
                    ),
                ),
                showHint = true
            )
        )
    }
}