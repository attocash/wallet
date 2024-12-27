package cash.atto.wallet.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Surface(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        color = Color.Transparent
    ) {
        ReceiveAttoContent(
            address = address.value.orEmpty(),
            onCopy = {
                clipboardManager.setText(
                    AnnotatedString(address.value.orEmpty())
                )
            }
        )
    }
}