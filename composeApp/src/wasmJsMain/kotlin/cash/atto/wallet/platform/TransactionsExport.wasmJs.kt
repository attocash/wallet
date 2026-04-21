package cash.atto.wallet.platform

import kotlinx.browser.document
import kotlinx.io.Buffer
import kotlinx.io.Sink
import kotlinx.io.readString
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsString

actual suspend fun exportCsvFile(
    fileName: String,
    writeCsv: suspend (Sink) -> Unit,
): CsvExportResult {
    val buffer = Buffer()
    writeCsv(buffer)
    val parts =
        JsArray<JsAny?>().also {
            it[0] = buffer.readString().toJsString()
        }
    val blob = Blob(parts, BlobPropertyBag(type = "text/csv;charset=utf-8"))
    val url = URL.createObjectURL(blob)
    val anchor = document.createElement("a") as HTMLAnchorElement
    anchor.href = url
    anchor.download = fileName
    document.body?.appendChild(anchor)
    anchor.click()
    document.body?.removeChild(anchor)
    URL.revokeObjectURL(url)

    return CsvExportResult(location = fileName)
}
