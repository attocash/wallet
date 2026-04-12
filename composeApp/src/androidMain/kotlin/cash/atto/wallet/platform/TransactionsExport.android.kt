package cash.atto.wallet.platform

import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.io.asSink
import kotlinx.io.buffered
import org.koin.core.context.GlobalContext

actual fun exportCsvFile(
    fileName: String,
    transactions: List<TransactionUiState>,
): CsvExportResult {
    val context = GlobalContext.get().get<android.content.Context>()
    val resolver = context.contentResolver
    val values =
        ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

    val uri =
        resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            ?: error("Failed to create CSV export.")

    val outputStream = resolver.openOutputStream(uri) ?: error("Failed to open CSV export stream.")
    val sink = outputStream.asSink().buffered()
    try {
        writeTransactionsCsv(sink, transactions)
    } finally {
        sink.close()
    }

    return CsvExportResult(location = "Downloads/$fileName")
}
