package cash.atto.wallet.platform

expect suspend fun shareText(text: String): Boolean

expect fun isShareAvailable(): Boolean
