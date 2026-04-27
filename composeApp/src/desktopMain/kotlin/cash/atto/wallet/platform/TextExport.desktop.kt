package cash.atto.wallet.platform

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.buffered
import kotlinx.io.writeString

actual suspend fun exportTextFile(
    fileName: String,
    mimeType: String,
    content: String,
): TextExportResult {
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
        sink.writeString(content)
    } finally {
        sink.close()
    }

    return TextExportResult(location = target.toString())
}
