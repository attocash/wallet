package cash.atto.wallet.platform

import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

actual suspend fun exportCsvFile(
    fileName: String,
    writeCsv: suspend (Sink) -> Unit,
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
        writeCsv(sink)
    } finally {
        sink.close()
    }

    return CsvExportResult(location = target.toString())
}
