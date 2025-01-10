package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.nio.charset.Charset

@Composable
actual fun QRCodeImage(
    modifier: Modifier,
    url: String,
    contentDescription: String
) {
    Box(Modifier.height(300.dp)) {
        try {
            val charset: Charset = Charset.forName("EUC_JP") // EUC_JP is supported
        } catch (ex: Exception) {
            return@Box
        }

        qrgenerator.QRCodeImage(
            modifier = Modifier
                .align(Alignment.Center)
                .then(modifier),
            url = String(url.toByteArray(Charsets.UTF_8)),
            contentDescription = "QR"
        )
    }
}