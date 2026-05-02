package cash.atto.wallet.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.time.Duration

internal suspend fun <T> retryWalletOperation(
    description: String,
    retryDelay: Duration,
    action: suspend () -> T,
): T {
    while (currentCoroutineContext().isActive) {
        try {
            return action()
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            println("Failed to $description: ${ex.message}")
            delay(retryDelay)
        }
    }
    throw CancellationException("Cancelled while trying to $description")
}
