package cash.atto.wallet.platform

import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@JsFun(
    """
    (text, onSuccess, onFailure) => {
        const navigator = globalThis.navigator;
        if (!navigator?.share) {
            console.log("Web Share unavailable: navigator.share missing");
            onFailure();
            return;
        }

        navigator.share({ text })
            .then(() => onSuccess())
            .catch((error) => {
                const name = error?.name ?? "Error";
                const message = error?.message ?? "Unknown share failure";
                console.log("Web Share failed:", name + ": " + message);
                onFailure();
            });
    }
    """,
)
private external fun shareTextJs(
    text: String,
    onSuccess: () -> Unit,
    onFailure: () -> Unit,
)

@JsFun(
    """
    () => {
        const navigator = globalThis.navigator;
        return globalThis.isSecureContext === true && !!navigator && typeof navigator.share === "function";
    }
    """,
)
private external fun isShareAvailableJs(): Boolean

actual suspend fun shareText(text: String): Boolean =
    suspendCoroutine { continuation ->
        shareTextJs(
            text = text,
            onSuccess = { continuation.resume(true) },
            onFailure = { continuation.resume(false) },
        )
    }

actual fun isShareAvailable(): Boolean = isShareAvailableJs()
