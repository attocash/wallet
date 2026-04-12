package cash.atto.wallet.platform

import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

actual fun exportCsvFile(
    fileName: String,
    transactions: List<TransactionUiState>,
): CsvExportResult {
    val home = System.getProperty("user.home")
    val downloads = Path(home, "Downloads")
    val targetDirectory =
        if (SystemFileSystem.exists(downloads)) {
            downloads
        } else {
            Path(home)
        }

    SystemFileSystem.createDirectories(targetDirectory)
    val target = Path(targetDirectory, fileName)
    val sink = SystemFileSystem.sink(target).buffered()
    try {
        writeTransactionsCsv(sink, transactions)
    } finally {
        sink.close()
    }

    return CsvExportResult(location = target.toString())
}
