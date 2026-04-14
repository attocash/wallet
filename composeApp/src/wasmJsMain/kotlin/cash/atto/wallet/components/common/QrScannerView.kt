package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
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
    var slotBounds by remember { mutableStateOf<Rect?>(null) }

    DisposableEffect(container) {
        container.style.position = "fixed"
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

    DisposableEffect(slotBounds, container) {
        slotBounds?.let { bounds ->
            container.style.left = "${bounds.left}px"
            container.style.top = "${bounds.top}px"
            container.style.width = "${bounds.width}px"
            container.style.height = "${bounds.height}px"
        }

        onDispose { }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(12.dp))
                .onGloballyPositioned { coordinates ->
                    slotBounds = coordinates.boundsInWindow()
                },
    )
}
