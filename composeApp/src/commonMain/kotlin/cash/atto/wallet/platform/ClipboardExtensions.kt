package cash.atto.wallet.platform

import androidx.compose.ui.platform.Clipboard

expect suspend fun Clipboard.setText(text: String)
