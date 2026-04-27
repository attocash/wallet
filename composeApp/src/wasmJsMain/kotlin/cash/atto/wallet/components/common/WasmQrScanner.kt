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
    private var onDebug: ((String) -> Unit)? = null
    private var scanTickCount = 0
    private var frameCaptureCount = 0
    private var decodeAttemptCount = 0
    private var lastFrameStatus: String? = null

    fun startScanning(
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onDebug: (String) -> Unit = {},
    ) {
        if (isScanning) return
        isScanning = true
        this.onDebug = onDebug
        scanTickCount = 0
        frameCaptureCount = 0
        decodeAttemptCount = 0
        lastFrameStatus = null

        loadZXingScript()
        emitDebug("Starting QR scanner")
        emitDebug("Decoder: bundled @zxing/library")
        emitDebug("Requesting camera with environment preference")

        val videoEl = createVideoElement()
        val canvasEl = createCanvasElement()
        videoElement = videoEl
        canvas = canvasEl

        getUserMedia(
            video = videoEl,
            onSuccess = {
                emitDebug("Camera stream started")
                emitDebug(describeActiveVideoTrack(videoEl))
                emitDebug(describeVideoState(videoEl))
                scanIntervalId =
                    setIntervalMs(
                        callback = {
                            if (!isScanning || !isZXingReady()) return@setIntervalMs

                            scanTickCount += 1

                            if (!drawVideoFrame(videoEl, canvasEl)) {
                                emitFrameStatusIfChanged(canvasEl)
                                if (scanTickCount % 12 == 0) {
                                    emitDebug(describeVideoState(videoEl))
                                }
                                return@setIntervalMs
                            }

                            frameCaptureCount += 1
                            emitFrameStatusIfChanged(canvasEl)

                            if (frameCaptureCount == 1) {
                                emitDebug("First frame captured, starting decode attempts")
                            }

                            decodeAttemptCount += 1

                            decodeWithZxing(
                                canvas = canvasEl,
                                onResult = { result ->
                                    emitDebug("QR code matched via ZXing")
                                    stopScanning()
                                    onResult(result)
                                },
                                onNoResult = {
                                    if (decodeAttemptCount % 12 == 0) {
                                        emitScanningSummary(videoEl)
                                    }
                                },
                            )
                        },
                        ms = 250,
                    )
            },
            onError = { error ->
                val message = error.toString()
                println("QR Scanner error: $message")
                emitDebug("Camera error: $message")
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
        onDebug = null
        lastFrameStatus = null
    }

    private fun emitFrameStatusIfChanged(canvas: HTMLCanvasElement) {
        val status = lastFrameDrawStatus(canvas)
        if (status != lastFrameStatus) {
            lastFrameStatus = status
            emitDebug(status)
        }
    }

    private fun emitDebug(message: String) {
        println("QR Scanner: $message")
        onDebug?.invoke(message)
    }

    private fun emitScanningSummary(video: HTMLVideoElement) {
        emitDebug(
            "Scanning... frames=$frameCaptureCount " +
                "decodeAttempts=$decodeAttemptCount " +
                describeVideoState(video),
        )
    }

    private fun decodeWithZxing(
        canvas: HTMLCanvasElement,
        onResult: (String) -> Unit,
        onNoResult: () -> Unit,
    ) {
        val result =
            decodeQrFromCanvas(
                canvas = canvas,
                onDecoderError = ::emitDebug,
            )
        if (result != null) {
            onResult(result)
            return
        }

        onNoResult()
    }
}
