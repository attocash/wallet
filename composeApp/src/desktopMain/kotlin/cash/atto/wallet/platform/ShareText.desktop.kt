package cash.atto.wallet.platform

actual suspend fun shareText(text: String): Boolean = false

actual fun isShareAvailable(): Boolean = false
