@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement

class WasmQrScanner : QrScanner {
    var videoElement: HTMLVideoElement? = null
        private set
    private var canvas: HTMLCanvasElement? = null
    private var scanIntervalId: Int? = null
    private var isScanning = false

    fun startScanning(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (isScanning) return
        isScanning = true

        loadZXingScript()

        val videoEl = createVideoElement()
        val canvasEl = createCanvasElement()
        videoElement = videoEl
        canvas = canvasEl

        getUserMedia(
            video = videoEl,
            onSuccess = {
                scanIntervalId =
                    setIntervalMs(
                        callback = {
                            if (!isScanning || !isZXingReady()) return@setIntervalMs
                            if (!drawVideoFrame(videoEl, canvasEl)) return@setIntervalMs

                            decodeWithZxing(
                                canvas = canvasEl,
                                onResult = { result ->
                                    stopScanning()
                                    onResult(result)
                                },
                                onNoResult = {},
                            )
                        },
                        ms = 250,
                    )
            },
            onError = { error ->
                val message = error.toString()
                println("QR Scanner error: $message")
                stopScanning()
                onError(message)
            },
        )
    }

    override fun startScanning(onResult: (String) -> Unit) {
        startScanning(
            onResult = onResult,
            onError = {},
        )
    }

    override fun stopScanning() {
        isScanning = false
        scanIntervalId?.let { clearIntervalId(it) }
        scanIntervalId = null
        videoElement?.let { stopMediaStream(it) }
        videoElement = null
        canvas = null
    }

    private fun decodeWithZxing(
        canvas: HTMLCanvasElement,
        onResult: (String) -> Unit,
        onNoResult: () -> Unit,
    ) {
        val result =
            decodeQrFromCanvas(
                canvas = canvas,
            )
        if (result != null) {
            onResult(result)
            return
        }

        onNoResult()
    }
}
