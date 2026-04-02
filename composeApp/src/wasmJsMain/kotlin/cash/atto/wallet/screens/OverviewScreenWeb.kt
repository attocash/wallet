package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverviewScreenWeb() {
    KoinContext {
        val overviewViewModel = koinViewModel<OverviewViewModel>()
        val overviewUiState = overviewViewModel.state.collectAsState()

        OverviewWeb(overviewUiState.value)
    }
}

@Composable
fun OverviewWeb(
    uiState: OverviewUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .safeDrawingPadding()
            .clip(RoundedCornerShape(50.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        if (uiState.pendingReceivableCount > 0) {
            Text(
                text = "${uiState.pendingReceivableCount} transactions awaiting receive" +
                    " · ${AttoFormatter.format(uiState.pendingReceivableAmount)} ATTO",
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        TransactionsList(
            uiState = uiState.transactionListUiState,
            modifier = Modifier.weight(1f),
            titleSize = 24.sp
        )
    }
}
