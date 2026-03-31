package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

@Composable
fun QrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit,
    onScanError: (String) -> Unit
) {
    var scannedValue by remember { mutableStateOf<String?>(null) }
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

        scanner.startScanning(
            onResult = { result ->
                scannedValue = result
                onQrCodeScanned(result)
                container.remove()
            },
            onError = { message ->
                onScanError(message)
                container.remove()
            }
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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (scannedValue != null) {
            Text(
                text = "Scanned: ${scannedValue}",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Text(
                text = "Scanning...",
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
