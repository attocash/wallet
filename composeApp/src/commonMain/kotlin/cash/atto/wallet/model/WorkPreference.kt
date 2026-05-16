package cash.atto.wallet.model

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val REMOTE_RECEIVER_RATE_LIMIT_SECONDS = 10
private const val LOCAL_RECEIVER_RATE_LIMIT_SECONDS = 0

@Serializable
data class WorkPreference(
    val source: WorkSourcePreference = WorkSourcePreference.REMOTE,
    val receiverRateLimitSeconds: Int = source.receiverRateLimitSeconds,
) {
    val receiverRateLimit: Duration
        get() = receiverRateLimitSeconds.seconds

    fun normalized(): WorkPreference = forSource(source)

    companion object {
        fun forSource(source: WorkSourcePreference): WorkPreference =
            WorkPreference(
                source = source,
                receiverRateLimitSeconds = source.receiverRateLimitSeconds,
            )
    }
}

@Serializable
enum class WorkSourcePreference {
    REMOTE,
    LOCAL,
}

private val WorkSourcePreference.receiverRateLimitSeconds: Int
    get() =
        when (this) {
            WorkSourcePreference.REMOTE -> REMOTE_RECEIVER_RATE_LIMIT_SECONDS
            WorkSourcePreference.LOCAL -> LOCAL_RECEIVER_RATE_LIMIT_SECONDS
        }
