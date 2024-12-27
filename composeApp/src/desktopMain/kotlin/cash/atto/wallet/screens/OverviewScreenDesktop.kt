package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cash.atto.wallet.components.overview.TransactionsList
import cash.atto.wallet.uistate.overview.OverviewUiState
import cash.atto.wallet.viewmodel.OverviewViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverviewScreenDesktop() {
    KoinContext {
        val overviewViewModel = koinViewModel<OverviewViewModel>()
        val overviewUiState = overviewViewModel.state.collectAsState()

        OverviewDesktop(overviewUiState.value)
    }
}

@Composable
fun OverviewDesktop(
    uiState: OverviewUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .safeDrawingPadding()
            .clip(RoundedCornerShape(50.dp))
            .background(color = MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        TransactionsList(
            uiState = uiState.transactionListUiState,
            modifier = Modifier.weight(1f),
            titleSize = 24.sp
        )
    }
}