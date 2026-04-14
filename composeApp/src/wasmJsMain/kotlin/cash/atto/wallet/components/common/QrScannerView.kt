package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

@Composable
fun qrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit,
    onScanError: (String) -> Unit,
) {
    val scanner = remember { WasmQrScanner() }
    val container = remember { document.createElement("div") as HTMLDivElement }

    DisposableEffect(container) {
        container.style.position = "fixed"
        container.style.left = "50%"
        container.style.top = "calc(50% + 36px)"
        container.style.transform = "translate(-50%, -50%)"
        container.style.width = "min(480px, calc(100vw - 64px))"
        container.style.height = "320px"
        container.style.maxHeight = "calc(100vh - 220px)"
        container.style.zIndex = "9999"
        container.style.backgroundColor = "black"
        container.style.borderRadius = "12px"
        container.style.setProperty("overflow", "hidden")
        container.style.setProperty("pointer-events", "none")
        container.id = "qr-scanner-container"
        document.body?.appendChild(container)

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
            videoEl.style.width = "100%"
            videoEl.style.height = "100%"
            videoEl.style.objectFit = "cover"
            container.appendChild(videoEl as Node)
        }

        onDispose {
            scanner.stopScanning()
            container.remove()
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(12.dp)),
    )
}
