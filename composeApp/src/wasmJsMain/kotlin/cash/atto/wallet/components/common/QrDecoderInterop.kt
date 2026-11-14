@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement
import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.JsString
import kotlin.js.Promise

@JsModule("zxing-cpp-qr")
internal external object QrDecoderModule : JsAny {
    fun initialize(): Promise<JsAny>

    fun decodeFromCanvas(canvas: HTMLCanvasElement): JsString?
}

internal fun initializeQrDecoder(
    onReady: () -> Unit,
    onError: (JsString) -> Unit,
): Unit = initializeQrDecoder(QrDecoderModule, onReady, onError)

private fun initializeQrDecoder(
    module: QrDecoderModule,
    onReady: () -> Unit,
    onError: (JsString) -> Unit,
): Unit =
    js(
        """
        {
            module.initialize().then(
                function () { onReady(); },
                function () { onError('Could not initialize QR decoder. Please try again.'); }
            );
        }
        """,
    )

internal fun isQrScannerSupported(): Boolean =
    js(
        """
    {
        var navigatorRef = globalThis.navigator;
        return (
            globalThis.isSecureContext === true &&
            !!navigatorRef &&
            !!navigatorRef.mediaDevices &&
            typeof navigatorRef.mediaDevices.getUserMedia === 'function'
        );
    }
""",
    )

internal fun decodeQrFromCanvas(
    canvas: HTMLCanvasElement,
    onDecoderError: (String) -> Unit = {},
): String? =
    try {
        QrDecoderModule.decodeFromCanvas(canvas)?.toString()?.takeIf(String::isNotBlank)
    } catch (_: Throwable) {
        onDecoderError("Decoder error")
        null
    }

internal fun createVideoElement(): HTMLVideoElement =
    js(
        """
    {
        var video = document.createElement('video');
        video.setAttribute('autoplay', '');
        video.setAttribute('playsinline', '');
        video.setAttribute('webkit-playsinline', 'true');
        video.setAttribute('muted', '');
        video.muted = true;
        video.style.display = 'block';
        video.style.width = '100%';
        video.style.height = '100%';
        video.style.objectFit = 'cover';
        return video;
    }
""",
    )

internal fun createCanvasElement(): HTMLCanvasElement =
    js(
        """
    {
        var canvas = document.createElement('canvas');
        canvas.style.display = 'none';
        return canvas;
    }
""",
    )

internal fun getUserMedia(
    video: HTMLVideoElement,
    onSuccess: () -> Unit,
    onError: (JsString) -> Unit,
): Unit =
    js(
        """
    {
        var constraints = {
            video: {
                facingMode: { ideal: 'environment' },
                width: { ideal: 1280 },
                height: { ideal: 1280 }
            }
        };
        function startStream(c) {
            return navigator.mediaDevices.getUserMedia(c)
                .then(function(stream) {
                    video.srcObject = stream;
                    return video.play();
                })
                .then(function() {
                    onSuccess();
                });
        }
        startStream(constraints)
            .catch(function() {
                return startStream({ video: true });
            })
            .catch(function(err) {
                onError(err.message || 'Camera access denied');
            });
    }
""",
    )

internal fun stopMediaStream(video: HTMLVideoElement): Unit =
    js(
        """
    {
        if (video.srcObject) {
            var tracks = video.srcObject.getTracks();
            for (var i = 0; i < tracks.length; i++) {
                tracks[i].stop();
            }
            video.srcObject = null;
        }
    }
""",
    )

internal fun drawVideoFrame(
    video: HTMLVideoElement,
    canvas: HTMLCanvasElement,
): Boolean =
    js(
        """
    {
        if (video.readyState < video.HAVE_CURRENT_DATA) return false;
        var vw = video.videoWidth;
        var vh = video.videoHeight;
        if (vw === 0 || vh === 0) return false;
        var maxDim = 720;
        if (vw > maxDim || vh > maxDim) {
            var scale = maxDim / Math.max(vw, vh);
            vw = Math.round(vw * scale);
            vh = Math.round(vh * scale);
        }
        canvas.width = vw;
        canvas.height = vh;
        if (canvas.width === 0 || canvas.height === 0) return false;
        var ctx = canvas.__attoCtx;
        if (!ctx) {
            ctx = canvas.getContext('2d', { willReadFrequently: true }) || canvas.getContext('2d');
            canvas.__attoCtx = ctx;
        }
        if (!ctx) return false;
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        return true;
    }
""",
    )

internal fun setIntervalMs(
    callback: () -> Unit,
    ms: Int,
): Int =
    js(
        """(setInterval(callback, ms))""",
    )

internal fun clearIntervalId(id: Int): Unit =
    js(
        """(clearInterval(id))""",
    )
