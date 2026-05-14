package cash.atto.wallet.model

import kotlinx.serialization.Serializable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private const val REMOTE_RATE_LIMIT_SECONDS = 10
private const val LOCAL_RATE_LIMIT_SECONDS = 0

@Serializable
data class WorkPreference(
    val source: WorkSourcePreference = WorkSourcePreference.REMOTE,
    val rateLimitSeconds: Int = source.rateLimitSeconds,
) {
    val rateLimit: Duration
        get() = rateLimitSeconds.seconds

    fun normalized(): WorkPreference = forSource(source)

    companion object {
        fun forSource(source: WorkSourcePreference): WorkPreference =
            WorkPreference(
                source = source,
                rateLimitSeconds = source.rateLimitSeconds,
            )
    }
}

@Serializable
enum class WorkSourcePreference {
    REMOTE,
    LOCAL,
}

private val WorkSourcePreference.rateLimitSeconds: Int
    get() =
        when (this) {
            WorkSourcePreference.REMOTE -> REMOTE_RATE_LIMIT_SECONDS
            WorkSourcePreference.LOCAL -> LOCAL_RATE_LIMIT_SECONDS
        }
