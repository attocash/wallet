package cash.atto.wallet.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import cash.atto.wallet.components.overview.ReceiveAttoContent
import cash.atto.wallet.viewmodel.ReceiveViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ReceiveScreenDesktop() {
    val viewModel = koinViewModel<ReceiveViewModel>()
    val address = viewModel.address

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    Surface(Modifier.padding(16.dp)) {
        ReceiveAttoContent(
            address = address.value.orEmpty(),
            onCopy = {
                clipboardManager.setText(
                    AnnotatedString(address.value.orEmpty())
                )
            },
            qrCodeSize = 256.dp
        )
    }
}