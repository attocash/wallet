package cash.atto.wallet.platform

import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.Clipboard

actual suspend fun Clipboard.setText(text: String) {
    setClipEntry(ClipEntry.withPlainText(text))
}
