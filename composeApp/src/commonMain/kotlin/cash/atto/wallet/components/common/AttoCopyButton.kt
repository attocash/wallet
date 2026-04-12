package cash.atto.wallet.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.dark_text_tertiary
import kotlinx.coroutines.delay

/**
 * A reusable copy-to-clipboard icon button with visual confirmation.
 *
 * Shows a ContentCopy icon by default. When clicked, copies [text] to the clipboard
 * and briefly shows a Check icon for [confirmationDurationMs] before reverting.
 *
 * @param text The string to copy to the clipboard.
 * @param modifier Modifier for the outer container.
 * @param size The size of the clickable area (icon is 2/3 of this).
 * @param tint The icon tint color.
 * @param confirmTint The tint for the check icon shown after copying.
 * @param contentDescription Accessibility description.
 * @param confirmationDurationMs How long the check icon is shown (default 1000ms).
 * @param onCopied Optional callback invoked after the text is copied.
 */
@Composable
fun AttoCopyButton(
    text: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = dark_text_tertiary,
    confirmTint: Color = Color(0xFF4CAF50),
    contentDescription: String? = "Copy",
    confirmationDurationMs: Long = 1000L,
    onCopied: (() -> Unit)? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(confirmationDurationMs)
            copied = false
        }
    }

    Box(
        modifier =
            modifier
                .size(size)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) {
                    clipboardManager.setText(AnnotatedString(text))
                    copied = true
                    onCopied?.invoke()
                },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
            contentDescription = contentDescription,
            tint = if (copied) confirmTint else tint,
            modifier = Modifier.size(size * 2 / 3),
        )
    }
}
