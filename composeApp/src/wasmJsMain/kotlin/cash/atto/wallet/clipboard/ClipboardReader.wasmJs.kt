package cash.atto.wallet.clipboard

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@JsFun(
    """
    (onSuccess, onError) => {
        const clipboard = globalThis.navigator?.clipboard;
        if (!clipboard?.readText) {
            onSuccess(null);
            return;
        }

        clipboard.readText()
            .then(text => onSuccess(text ?? null))
            .catch(() => onError());
    }
    """,
)
private external fun readClipboardTextJs(
    onSuccess: (String?) -> Unit,
    onError: () -> Unit,
)

actual suspend fun readClipboardText(): String? =
    suspendCoroutine { continuation ->
        readClipboardTextJs(
            onSuccess = { continuation.resume(it) },
            onError = { continuation.resume(null) },
        )
    }
