@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import org.w3c.dom.HTMLVideoElement

class WasmQrScanner : QrScanner {
    var videoElement: HTMLVideoElement? = null
        private set
    private var scanIntervalId: Int? = null
    private var isScanning = false

    fun startScanning(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
    ) {
        if (isScanning) return
        isScanning = true

        val videoEl = createVideoElement()
        val canvasEl = createCanvasElement()
        videoElement = videoEl

        initializeQrDecoder(
            onReady = {
                if (isScanning && videoElement === videoEl) {
                    getUserMedia(
                        video = videoEl,
                        onSuccess = {
                            if (!isScanning || videoElement !== videoEl) {
                                stopMediaStream(videoEl)
                            } else {
                                scanIntervalId =
                                    setIntervalMs(
                                        callback = {
                                            if (!isScanning || videoElement !== videoEl) return@setIntervalMs
                                            if (!drawVideoFrame(videoEl, canvasEl)) return@setIntervalMs
                                            val result = decodeQrFromCanvas(canvasEl)
                                            if (result != null) {
                                                stopScanning()
                                                onResult(result)
                                            }
                                        },
                                        ms = 250,
                                    )
                            }
                        },
                        onError = { error ->
                            if (isScanning && videoElement === videoEl) {
                                stopScanning()
                                onError(error.toString())
                            }
                        },
                    )
                }
            },
            onError = { error ->
                if (isScanning && videoElement === videoEl) {
                    stopScanning()
                    onError(error.toString())
                }
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
    }
}
