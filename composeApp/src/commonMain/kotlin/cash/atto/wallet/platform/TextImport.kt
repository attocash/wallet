package cash.atto.wallet.platform

data class TextImportResult(
    val location: String,
    val content: String,
)

expect suspend fun importTextFile(
    mimeTypes: List<String>,
    extensions: List<String>,
): TextImportResult?
