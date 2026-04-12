package cash.atto.wallet.platform

import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual fun exportCsvFile(
    fileName: String,
    transactions: List<TransactionUiState>,
): CsvExportResult {
    val documentsDirectory =
        NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        )?.path ?: error("Unable to access the Documents directory.")

    val targetPath = Path(documentsDirectory, fileName)
    val sink = SystemFileSystem.sink(targetPath).buffered()
    try {
        writeTransactionsCsv(sink, transactions)
    } finally {
        sink.close()
    }

    return CsvExportResult(location = targetPath.toString())
}
