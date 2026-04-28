@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package cash.atto.wallet.components.common

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement
import kotlin.js.JsAny
import kotlin.js.JsModule
import kotlin.js.JsString

@JsModule("@zxing/library")
external object ZXingModule : JsAny

internal fun loadZXingScript() = Unit

internal fun isZXingReady(): Boolean = true

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
): String? {
    val width = canvas.width
    val height = canvas.height
    if (width == 0 || height == 0) return null

    val luminanceData = canvasLuminanceData(canvas) ?: return null

    return try {
        val source =
            createRGBLuminanceSource(
                module = ZXingModule,
                luminances = luminanceData,
                width = width,
                height = height,
            )
        val bitmap =
            createBinaryBitmap(
                module = ZXingModule,
                source = source,
            )
        decodeBitmapOrNull(
            module = ZXingModule,
            bitmap = bitmap,
        )?.takeIf(String::isNotBlank)
    } catch (error: Throwable) {
        val message = error.message ?: error.toString()
        onDecoderError("Decoder error: $message")
        println("QR decode error: $message")
        null
    }
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
        if (video.readyState < video.HAVE_CURRENT_DATA) {
            canvas.__attoLastDrawStatus = 'Waiting for frame data (readyState=' + video.readyState + ')';
            return false;
        }
        var vw = video.videoWidth;
        var vh = video.videoHeight;
        if (vw === 0 || vh === 0) {
            canvas.__attoLastDrawStatus = 'Video dimensions not ready yet';
            return false;
        }
        var maxDim = 720;
        if (vw > maxDim || vh > maxDim) {
            var scale = maxDim / Math.max(vw, vh);
            vw = Math.round(vw * scale);
            vh = Math.round(vh * scale);
        }
        canvas.width = vw;
        canvas.height = vh;
        if (canvas.width === 0 || canvas.height === 0) {
            canvas.__attoLastDrawStatus = 'Canvas dimensions collapsed to zero';
            return false;
        }
        var ctx = canvas.__attoCtx;
        if (!ctx) {
            ctx = canvas.getContext('2d', { willReadFrequently: true }) || canvas.getContext('2d');
            canvas.__attoCtx = ctx;
        }
        if (!ctx) {
            canvas.__attoLastDrawStatus = '2D canvas context unavailable';
            return false;
        }
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        canvas.__attoLastDrawStatus =
            'Frame captured video=' + video.videoWidth + 'x' + video.videoHeight +
            ' canvas=' + canvas.width + 'x' + canvas.height +
            ' readyState=' + video.readyState;
        return true;
    }
""",
    )

internal fun lastFrameDrawStatus(canvas: HTMLCanvasElement): String =
    js(
        """(canvas.__attoLastDrawStatus || "No frame status yet")""",
    )

internal fun describeActiveVideoTrack(video: HTMLVideoElement): String =
    js(
        """
    {
        if (!video.srcObject) return 'Camera stream attached, track details unavailable';
        var tracks = video.srcObject.getVideoTracks ? video.srcObject.getVideoTracks() : [];
        if (!tracks.length) return 'Camera stream has no video tracks';
        var track = tracks[0];
        var settings = track.getSettings ? track.getSettings() : {};
        var label = track.label || 'unknown';
        var facingMode = settings.facingMode || 'unknown';
        var width = settings.width || '?';
        var height = settings.height || '?';
        return 'Track=' + label + ' facing=' + facingMode + ' size=' + width + 'x' + height;
    }
""",
    )

internal fun describeVideoState(video: HTMLVideoElement): String =
    js(
        """
    {
        return 'Video readyState=' + video.readyState + ' size=' + (video.videoWidth || 0) + 'x' + (video.videoHeight || 0);
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

private fun canvasLuminanceData(canvas: HTMLCanvasElement): JsAny? =
    js(
        """
    {
        var ctx = canvas.__attoCtx || canvas.getContext('2d');
        if (!ctx) return null;
        var width = canvas.width;
        var height = canvas.height;
        if (width === 0 || height === 0) return null;
        var rgba = ctx.getImageData(0, 0, width, height).data;
        var len = width * height;
        var luminances = new Uint8ClampedArray(len);
        for (var i = 0; i < len; i++) {
            var offset = i * 4;
            luminances[i] =
                ((rgba[offset] * 306 + rgba[offset + 1] * 601 + rgba[offset + 2] * 117 + 0x200) >> 10);
        }
        return luminances;
    }
""",
    )

private fun createRGBLuminanceSource(
    module: ZXingModule,
    luminances: JsAny,
    width: Int,
    height: Int,
): JsAny =
    js(
        """
    new module.RGBLuminanceSource(luminances, width, height)
""",
    )

private fun createBinaryBitmap(
    module: ZXingModule,
    source: JsAny,
): JsAny =
    js(
        """
    new module.BinaryBitmap(new module.HybridBinarizer(source))
""",
    )

private fun decodeBitmapOrNull(
    module: ZXingModule,
    bitmap: JsAny,
): String? =
    js(
        """
    {
        var hints = new Map();
        hints.set(module.DecodeHintType.TRY_HARDER, true);
        try {
            var reader = new module.QRCodeReader();
            var result = reader.decode(bitmap, hints);
            return result ? result.getText() : null;
        } catch (error) {
            if (error && error.name === 'NotFoundException') return null;
            throw error;
        }
    }
""",
    )
