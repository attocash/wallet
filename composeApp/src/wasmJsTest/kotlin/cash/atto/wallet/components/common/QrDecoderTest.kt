@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import io.github.alexzhirkevich.qrose.QroseEncoders
import io.github.alexzhirkevich.qrose.matrix.QR
import kotlinx.browser.document
import kotlinx.coroutines.await
import kotlinx.coroutines.test.runTest
import org.w3c.dom.HTMLCanvasElement
import kotlin.js.JsAny
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QrDecoderTest {
    @Test
    fun `concurrent initialization shares one upstream wasm instance`() =
        runTest {
            // Given
            val first = QrDecoderModule.initialize()
            val second = QrDecoderModule.initialize()

            // When
            val firstModule = first.await<JsAny>()
            val secondModule = second.await<JsAny>()

            // Then
            assertTrue(sameInstance(firstModule, secondModule))
        }

    @Test
    fun `decodes generated payment and text QR codes at different scales and rotations`() =
        runTest {
            // Given
            QrDecoderModule.initialize().await<JsAny>()
            val payment = "atto://aarfzz26z5pfwrkdcwt4jdhhe2vvixscqwehgmfjqxku43rgtjso5p5cjw6fw?amount=1234567890"
            val text = "Atto QR decoder - portable browser text"
            val fixtures = listOf(Triple(payment, 3, false), Triple(payment, 6, true), Triple(text, 4, true), Triple(text, 7, false))

            for ((payload, scale, rotated) in fixtures) {
                val canvas = qrCanvas(payload, scale, rotated)

                // When
                val decoded = decodeQrFromCanvas(canvas)

                // Then
                assertEquals(payload, decoded, "scale=$scale, rotated=$rotated")
            }
        }

    @Test
    fun `empty canvas and image without a QR code produce no result`() =
        runTest {
            // Given
            QrDecoderModule.initialize().await<JsAny>()
            val blank = document.createElement("canvas") as HTMLCanvasElement
            blank.width = 200
            blank.height = 150
            paintSquare(blank, 0, 0, 200, false)
            val empty = document.createElement("canvas") as HTMLCanvasElement
            empty.width = 0
            empty.height = 0

            // When
            val blankResult = decodeQrFromCanvas(blank)
            val emptyResult = decodeQrFromCanvas(empty)

            // Then
            assertNull(blankResult)
            assertNull(emptyResult)
        }

    @Test
    fun `frame allocations are released after successful decoding and decoder errors`() =
        runTest {
            // Given
            val module = QrDecoderModule.initialize().await<JsAny>()
            val canvas = qrCanvas("frame memory lifecycle", 4, false)
            installAllocationProbe(module)
            try {
                // When
                repeat(20) { assertEquals("frame memory lifecycle", decodeQrFromCanvas(canvas)) }

                // Then
                assertEquals(20, allocationCount(module))
                assertEquals(0, outstandingAllocations(module))

                // When
                failNextDecode(module)
                var errors = 0
                val result = decodeQrFromCanvas(canvas) { errors++ }

                // Then
                assertNull(result)
                assertEquals(1, errors)
                assertEquals(21, allocationCount(module))
                assertEquals(0, outstandingAllocations(module))
            } finally {
                removeAllocationProbe(module)
            }
        }

    @Test
    fun `stopping before async decoder readiness never requests the camera`() =
        runTest {
            // Given
            QrDecoderModule.initialize().await<JsAny>()
            val camera = installCameraProbe()
            val scanner = WasmQrScanner()
            try {
                // When
                scanner.startScanning({ error("stopped scanner must not decode") }, { error(it) })
                scanner.stopScanning()
                QrDecoderModule.initialize().await<JsAny>()

                // Then
                assertEquals(0, cameraRequestCount(camera))
                assertNull(scanner.videoElement)
            } finally {
                scanner.stopScanning()
                removeCameraProbe(camera)
            }
        }

    private fun qrCanvas(
        payload: String,
        scale: Int,
        rotated: Boolean,
    ): HTMLCanvasElement {
        val matrix = QroseEncoders.QR().encode(payload)
        val canvas = document.createElement("canvas") as HTMLCanvasElement
        val quietZone = 4
        val size = (matrix.width + quietZone * 2) * scale
        canvas.width = size
        canvas.height = size
        paintSquare(canvas, 0, 0, size, false)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {
                    val pixelX = if (rotated) matrix.height - y - 1 else x
                    val pixelY = if (rotated) x else y
                    paintSquare(canvas, (pixelX + quietZone) * scale, (pixelY + quietZone) * scale, scale, true)
                }
            }
        }
        return canvas
    }
}

private fun paintSquare(
    canvas: HTMLCanvasElement,
    x: Int,
    y: Int,
    size: Int,
    dark: Boolean,
): Unit =
    js("{ const context = canvas.getContext('2d'); context.fillStyle = dark ? 'black' : 'white'; context.fillRect(x, y, size, size); }")

private fun sameInstance(
    first: JsAny,
    second: JsAny,
): Boolean = js("first === second")

private fun installAllocationProbe(module: JsAny): Unit =
    js(
        """
        {
        const probe = module.__walletProbe = {malloc: module._malloc, free: module._free, read: module.readBarcodeFromPixmap, count: 0, live: new Set()};
        module._malloc = function (size) { const pointer = probe.malloc(size); probe.count++; probe.live.add(pointer); return pointer; };
        module._free = function (pointer) { probe.live.delete(pointer); probe.free(pointer); };
        }
        """,
    )

private fun allocationCount(module: JsAny): Int = js("module.__walletProbe.count")

private fun outstandingAllocations(module: JsAny): Int = js("module.__walletProbe.live.size")

private fun failNextDecode(module: JsAny): Unit =
    js("{ module.readBarcodeFromPixmap = function () { throw new Error('simulated decoder exception after allocation'); }; }")

private fun removeAllocationProbe(module: JsAny): Unit =
    js(
        """
        {
        const probe = module.__walletProbe;
        module._malloc = probe.malloc; module._free = probe.free; module.readBarcodeFromPixmap = probe.read;
        delete module.__walletProbe;
        }
        """,
    )

private fun installCameraProbe(): JsAny =
    js(
        """
        {
            const mediaDevices = navigator.mediaDevices;
            const probe = {original: mediaDevices.getUserMedia, calls: 0};
            mediaDevices.getUserMedia = function () { probe.calls++; return Promise.reject(new Error('unexpected camera request')); };
            return probe;
        }
        """,
    )

private fun cameraRequestCount(probe: JsAny): Int = js("probe.calls")

private fun removeCameraProbe(probe: JsAny): Unit = js("{ navigator.mediaDevices.getUserMedia = probe.original; }")
