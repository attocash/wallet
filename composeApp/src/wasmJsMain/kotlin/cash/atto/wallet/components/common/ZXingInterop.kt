package cash.atto.wallet.components.common

import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.HTMLVideoElement

internal fun loadZXingScript(): Unit = js(
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
"""
)

internal fun isZXingReady(): Boolean = js(
    """(typeof window.__zxingLoaded !== 'undefined' && window.__zxingLoaded === true)"""
)

internal fun decodeQrFromCanvas(canvas: HTMLCanvasElement): JsString? = js(
    """
    {
        try {
            var ZXingLib = window.ZXing || (typeof ZXing !== 'undefined' ? ZXing : null);
            if (!ZXingLib) return null;
            var source = new ZXingLib.HTMLCanvasElementLuminanceSource(canvas);
            var binarizer = new ZXingLib.HybridBinarizer(source);
            var bitmap = new ZXingLib.BinaryBitmap(binarizer);
            var reader = new ZXingLib.QRCodeReader();
            var result = reader.decode(bitmap);
            return result ? result.getText() : null;
        } catch (e) {
            return null;
        }
    }
"""
)

internal fun createVideoElement(): HTMLVideoElement = js(
    """
    {
        var video = document.createElement('video');
        video.setAttribute('autoplay', '');
        video.setAttribute('playsinline', '');
        video.style.position = 'absolute';
        video.style.top = '0';
        video.style.left = '0';
        video.style.width = '100%';
        video.style.height = '100%';
        video.style.objectFit = 'cover';
        return video;
    }
"""
)

internal fun createCanvasElement(): HTMLCanvasElement = js(
    """
    {
        var canvas = document.createElement('canvas');
        canvas.style.display = 'none';
        return canvas;
    }
"""
)

internal fun getUserMedia(video: HTMLVideoElement, onSuccess: () -> Unit, onError: (JsString) -> Unit): Unit = js(
    """
    {
        var constraints = { video: { facingMode: { ideal: 'environment' } } };
        navigator.mediaDevices.getUserMedia(constraints)
            .then(function(stream) {
                video.srcObject = stream;
                return video.play();
            })
            .then(function() {
                onSuccess();
            })
            .catch(function(err) {
                onError(err.message || 'Camera access denied');
            });
    }
"""
)

internal fun stopMediaStream(video: HTMLVideoElement): Unit = js(
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
"""
)

internal fun drawVideoFrame(video: HTMLVideoElement, canvas: HTMLCanvasElement): Boolean = js(
    """
    {
        if (video.readyState !== video.HAVE_ENOUGH_DATA) return false;
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        if (canvas.width === 0 || canvas.height === 0) return false;
        var ctx = canvas.getContext('2d');
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
        return true;
    }
"""
)

internal fun setIntervalMs(callback: () -> Unit, ms: Int): Int = js(
    """(setInterval(callback, ms))"""
)

internal fun clearIntervalId(id: Int): Unit = js(
    """(clearInterval(id))"""
)
