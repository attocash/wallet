package cash.atto.wallet.platform

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@JsFun(
    """
    (text, onSuccess, onFailure) => {
        const share = globalThis.navigator?.share;
        if (!share) {
            onFailure();
            return;
        }

        share({ text })
            .then(() => onSuccess())
            .catch(() => onFailure());
    }
    """,
)
private external fun shareTextJs(
    text: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
)

actual suspend fun shareText(text: String): Boolean =
    suspendCoroutine { continuation ->
        shareTextJs(
            text = text,
            onSuccess = { continuation.resume(true) },
            onFailure = { continuation.resume(false) },
        )
    }
