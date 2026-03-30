package cash.atto.wallet.components.common

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement

class WasmQrScanner : QrScanner {

    var videoElement: HTMLVideoElement? = null
        private set
    private var canvas: HTMLCanvasElement? = null
    private var scanIntervalId: Int? = null
    private var isScanning = false

    override fun startScanning(onResult: (String) -> Unit) {
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
                scanIntervalId = setIntervalMs(
                    callback = {
                        if (!isZXingReady()) return@setIntervalMs
                        if (!drawVideoFrame(videoEl, canvasEl)) return@setIntervalMs
                        val result = decodeQrFromCanvas(canvasEl)
                        if (result != null) {
                            val text = result.toString()
                            if (text.isNotEmpty()) {
                                stopScanning()
                                onResult(text)
                            }
                        }
                    },
                    ms = 250
                )
            },
            onError = { error ->
                println("QR Scanner error: ${error.toString()}")
                stopScanning()
            }
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
}
