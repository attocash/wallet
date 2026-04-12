package cash.atto.wallet.components.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
expect fun qrCodeImage(
    modifier: Modifier = Modifier,
    url: String,
    contentDescription: String,
)

@Preview
@Composable
fun QrCodeImagePreview() {
    AttoWalletTheme {
        qrCodeImage(
            url = "atto://adlhpy2hjrmc65bnbrzbxkwsq5h7payxmjf3xmiob5npvldjkkbpapo34ubvo",
            contentDescription = "QR",
        )
    }
}
