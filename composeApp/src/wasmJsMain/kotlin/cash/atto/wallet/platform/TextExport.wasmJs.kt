package cash.atto.wallet.platform

import kotlinx.browser.document
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsString

actual suspend fun exportTextFile(
    fileName: String,
    mimeType: String,
    content: String,
): TextExportResult {
    val parts =
        JsArray<JsAny?>().also {
            it[0] = content.toJsString()
        }
    val blob = Blob(parts, BlobPropertyBag(type = mimeType))
    val url = URL.createObjectURL(blob)
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    document.body?.appendChild(anchor)
    anchor.click()
    document.body?.removeChild(anchor)
    URL.revokeObjectURL(url)

    return TextExportResult(location = fileName)
}
