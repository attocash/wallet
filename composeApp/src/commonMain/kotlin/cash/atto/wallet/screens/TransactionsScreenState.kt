package cash.atto.wallet.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.TransactionUiState

@Composable
fun rememberTransactionsScreenState(): TransactionsScreenState = remember { TransactionsScreenState() }

@Stable
class TransactionsScreenState {
    var selectedTransaction by mutableStateOf<TransactionUiState?>(null)
        private set

    var isFilterDialogVisible by mutableStateOf(false)
        private set

    var selectedTypes by mutableStateOf(TransactionType.entries.toSet())
        private set

    var exportMessage by mutableStateOf<String?>(null)
        private set

    val isFilterActive: Boolean
        get() = selectedTypes.size != TransactionType.entries.size

    fun selectTransaction(transaction: TransactionUiState) {
        selectedTransaction = transaction
    }

    fun dismissTransaction() {
        selectedTransaction = null
    }

    fun showFilterDialog() {
        isFilterDialogVisible = true
    }

    fun dismissFilterDialog() {
        isFilterDialogVisible = false
    }

    fun applyFilter(types: Set<TransactionType>) {
        selectedTypes = types
        isFilterDialogVisible = false
    }

    fun showExportMessage(message: String) {
        exportMessage = message
    }

    fun clearExportMessage() {
        exportMessage = null
    }
}
