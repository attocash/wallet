package cash.atto.wallet.platform

actual suspend fun importTextFile(
    @Suppress("UNUSED_PARAMETER") mimeTypes: List<String>,
    @Suppress("UNUSED_PARAMETER") extensions: List<String>,
): TextImportResult? = error("Importing preferences is not supported on Android yet.")
