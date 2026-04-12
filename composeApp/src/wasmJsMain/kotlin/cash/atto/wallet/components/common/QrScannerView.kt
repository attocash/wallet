package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.browser.document
import org.w3c.dom.HTMLButtonElement
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

@Composable
fun qrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit,
    onScanError: (String) -> Unit,
    onDismiss: () -> Unit = {},
) {
    val scanner = remember { WasmQrScanner() }

    DisposableEffect(Unit) {
        val container = document.createElement("div") as HTMLDivElement
        container.style.position = "fixed"
        container.style.top = "0"
        container.style.left = "0"
        container.style.width = "100vw"
        container.style.height = "100vh"
        container.style.zIndex = "9999"
        container.style.backgroundColor = "black"
        container.id = "qr-scanner-container"
        document.body?.appendChild(container)

        // Cancel button as an HTML element so it renders on top of the video
        val cancelBtn = document.createElement("button") as HTMLButtonElement
        cancelBtn.textContent = "Cancel"
        cancelBtn.style.position = "absolute"
        cancelBtn.style.bottom = "32px"
        cancelBtn.style.left = "50%"
        cancelBtn.style.transform = "translateX(-50%)"
        cancelBtn.style.zIndex = "10000"
        cancelBtn.style.padding = "12px 32px"
        cancelBtn.style.fontSize = "16px"
        cancelBtn.style.color = "white"
        cancelBtn.style.backgroundColor = "rgba(255,255,255,0.2)"
        cancelBtn.style.border = "1px solid rgba(255,255,255,0.4)"
        cancelBtn.style.borderRadius = "8px"
        cancelBtn.style.cursor = "pointer"
        cancelBtn.onclick = {
            scanner.stopScanning()
            container.remove()
            onDismiss()
            null
        }
        container.appendChild(cancelBtn)

        scanner.startScanning(
            onResult = { result ->
                onQrCodeScanned(result)
                container.remove()
            },
            onError = { message ->
                onScanError(message)
                container.remove()
            },
        )

        val videoEl = scanner.videoElement
        if (videoEl != null) {
            container.appendChild(videoEl as Node)
        }

        onDispose {
            scanner.stopScanning()
            container.remove()
        }
    }

    Box(modifier = modifier.fillMaxSize())
}
