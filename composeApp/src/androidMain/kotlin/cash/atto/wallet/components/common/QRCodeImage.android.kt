package cash.atto.wallet.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun QRCodeImage(
    modifier: Modifier,
    url: String,
    contentDescription: String
) {
    qrgenerator.QRCodeImage(
        modifier = modifier,
        url = url,
        contentDescription = contentDescription
    )
}