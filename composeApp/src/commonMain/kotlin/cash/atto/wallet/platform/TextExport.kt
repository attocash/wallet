package cash.atto.wallet.platform

data class TextExportResult(
    val location: String,
)

expect suspend fun exportTextFile(
    fileName: String,
    mimeType: String,
    content: String,
): TextExportResult
