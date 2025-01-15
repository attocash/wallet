package cash.atto.wallet.components.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.rememberQrCodePainter

@Composable
actual fun QRCodeImage(
    modifier: Modifier,
    url: String,
    contentDescription: String
) {
    if (url.isNotEmpty()) {
        Image(
            modifier = modifier.padding(16.dp),
            painter = rememberQrCodePainter(url),
            contentDescription = contentDescription
        )
    }
}