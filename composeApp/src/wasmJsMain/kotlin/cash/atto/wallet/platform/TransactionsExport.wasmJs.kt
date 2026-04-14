package cash.atto.wallet.platform

import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.browser.document
import kotlinx.io.Buffer
import kotlinx.io.readString
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.js.JsAny
import kotlin.js.JsArray
import kotlin.js.toJsString

actual fun exportCsvFile(
    fileName: String,
    transactions: List<TransactionUiState>,
): CsvExportResult {
    val buffer = Buffer()
    writeTransactionsCsv(buffer, transactions)
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
