package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.overview.OverviewHeader
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

        OverviewDesktop(overviewUiState.value,)
    }
}

@Composable
fun OverviewDesktop(
    uiState: OverviewUiState
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.surface)
            .safeDrawingPadding()
            .padding(16.dp)
    ) {
        OverviewHeader(
            uiState = uiState.headerUiState,
            modifier = Modifier.fillMaxWidth()
        )

        TransactionsList(
            uiState = uiState.transactionListUiState,
            modifier = Modifier.weight(1f)
        )
    }
}