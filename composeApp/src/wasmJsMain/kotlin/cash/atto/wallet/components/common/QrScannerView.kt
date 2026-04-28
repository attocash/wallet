package cash.atto.wallet.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.browser.document
import org.w3c.dom.HTMLDivElement
import org.w3c.dom.Node

private const val qrScannerVideoHeightPx = 320
private const val qrScannerDebugHeightPx = 128

@Composable
fun QrScannerView(
    modifier: Modifier = Modifier,
    onQrCodeScanned: (String) -> Unit,
    onScanError: (String) -> Unit,
) {
    val scanner = remember { WasmQrScanner() }
    val overlayRoot = remember { document.createElement("div") as HTMLDivElement }
    val videoContainer = remember { document.createElement("div") as HTMLDivElement }
    val debugContainer = remember { document.createElement("div") as HTMLDivElement }
    val debugLines = remember { mutableStateListOf("Preparing scanner...") }

    fun renderDebugLines() {
        debugContainer.textContent =
            buildString {
                appendLine("Scanner Debug")
                appendLine()
                debugLines.takeLast(8).forEach { line -> appendLine(line) }
            }
    }

    fun appendDebugLine(message: String) {
        val normalized = message.trim()
        if (normalized.isBlank() || debugLines.lastOrNull() == normalized) return

        debugLines.add(normalized)
        while (debugLines.size > 12) {
            debugLines.removeAt(0)
        }

        renderDebugLines()
    }

    DisposableEffect(overlayRoot) {
        overlayRoot.style.position = "fixed"
        overlayRoot.style.left = "50%"
        overlayRoot.style.top = "calc(50% + 48px)"
        overlayRoot.style.transform = "translate(-50%, -50%)"
        overlayRoot.style.width = "min(480px, calc(100vw - 64px))"
        overlayRoot.style.maxWidth = "calc(100vw - 64px)"
        overlayRoot.style.display = "flex"
        overlayRoot.style.flexDirection = "column"
        overlayRoot.style.setProperty("gap", "12px")
        overlayRoot.style.zIndex = "9999"
        overlayRoot.style.setProperty("pointer-events", "none")
        overlayRoot.id = "qr-scanner-overlay"

        videoContainer.style.height = "${qrScannerVideoHeightPx}px"
        videoContainer.style.maxHeight = "calc(100vh - 280px)"
        videoContainer.style.backgroundColor = "black"
        videoContainer.style.borderRadius = "12px"
        videoContainer.style.setProperty("overflow", "hidden")

        debugContainer.style.minHeight = "${qrScannerDebugHeightPx}px"
        debugContainer.style.padding = "14px 16px"
        debugContainer.style.borderRadius = "12px"
        debugContainer.style.backgroundColor = "#1A1A1D"
        debugContainer.style.border = "1px solid #2C2C2F"
        debugContainer.style.color = "#A0A0A0"
        debugContainer.style.fontFamily = "monospace"
        debugContainer.style.fontSize = "12px"
        debugContainer.style.lineHeight = "1.45"
        debugContainer.style.whiteSpace = "pre-wrap"
        debugContainer.style.wordBreak = "break-word"
        debugContainer.style.boxSizing = "border-box"

        renderDebugLines()

        overlayRoot.appendChild(videoContainer)
        overlayRoot.appendChild(debugContainer)
        document.body?.appendChild(overlayRoot)

        scanner.startScanning(
            onResult = { result ->
                onQrCodeScanned(result)
                overlayRoot.remove()
            },
            onError = { message ->
                onScanError(message)
                overlayRoot.remove()
            },
            onDebug = ::appendDebugLine,
        )

        val videoEl = scanner.videoElement
        if (videoEl != null) {
            videoEl.style.width = "100%"
            videoEl.style.height = "100%"
            videoEl.style.objectFit = "cover"
            videoContainer.appendChild(videoEl as Node)
        }

        onDispose {
            scanner.stopScanning()
            overlayRoot.remove()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(qrScannerVideoHeightPx.dp),
        )

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(qrScannerDebugHeightPx.dp),
        )
    }
}
