package cash.atto.wallet.components.common

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement

internal fun loadZXingScript(): Unit =
    js(
        """
    {
        if (window.__zxingLoaded) return;
        if (window.__zxingLoading) return;
        window.__zxingLoading = true;
        var script = document.createElement('script');
        script.src = 'https://unpkg.com/@zxing/library@0.21.3/umd/index.min.js';
        script.onload = function() {
            window.__zxingLoaded = true;
            window.__zxingLoading = false;
        };
        script.onerror = function() {
            console.error('Failed to load ZXing library');
            window.__zxingLoading = false;
        };
        document.head.appendChild(script);
    }
""",
    )

internal fun isZXingReady(): Boolean =
    js(
        """(typeof window.__zxingLoaded !== 'undefined' && window.__zxingLoaded === true)""",
    )

internal fun decodeQrFromCanvas(canvas: HTMLCanvasElement): JsString? =
    js(
        """
    {
        try {
            var ZXingLib = window.ZXing || (typeof ZXing !== 'undefined' ? ZXing : null);
            if (!ZXingLib) return null;
            var ctx = canvas.__attoCtx || canvas.getContext('2d');
            if (!ctx) return null;
            var width = canvas.width;
            var height = canvas.height;
            if (width === 0 || height === 0) return null;
            var imageData = ctx.getImageData(0, 0, width, height);
            var data = imageData.data;
            var len = width * height;
            var luminances = new Uint8ClampedArray(len);
            for (var i = 0; i < len; i++) {
                var offset = i * 4;
                luminances[i] = ((data[offset] * 306 + data[offset + 1] * 601 + data[offset + 2] * 117 + 0x200) >> 10);
            }
            var source = new ZXingLib.RGBLuminanceSource(luminances, width, height);
            var binarizer = new ZXingLib.HybridBinarizer(source);
            var bitmap = new ZXingLib.BinaryBitmap(binarizer);
            var hints = new Map();
            hints.set(ZXingLib.DecodeHintType.TRY_HARDER, true);
            var reader = new ZXingLib.QRCodeReader();
            var result = reader.decode(bitmap, hints);
            return result ? result.getText() : null;
        } catch (e) {
            if (e.name && e.name !== 'NotFoundException') {
                console.error('QR decode error:', e);
            }
            return null;
        }
    }
""",
    )

internal fun createVideoElement(): HTMLVideoElement =
    js(
        """
    {
        var video = document.createElement('video');
        video.setAttribute('autoplay', '');
        video.setAttribute('playsinline', '');
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
        var constraints = { video: { facingMode: { ideal: 'environment' } } };
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
            .catch(function(err) {
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
        if (video.readyState !== video.HAVE_ENOUGH_DATA) return false;
        var vw = video.videoWidth;
        var vh = video.videoHeight;
        if (vw === 0 || vh === 0) return false;
        var maxDim = 640;
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
