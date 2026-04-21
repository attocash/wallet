package cash.atto.wallet.platform

import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.io.Sink
import kotlinx.io.writeString

data class CsvExportResult(
    val location: String,
)

expect suspend fun exportCsvFile(
    fileName: String,
    writeCsv: suspend (Sink) -> Unit,
): CsvExportResult

internal fun writeTransactionsCsv(
    sink: Sink,
    transactions: List<TransactionUiState>,
) {
    writeTransactionsCsvHeader(sink)
    appendTransactionsCsvRows(sink, transactions)
    sink.flush()
}

internal fun writeTransactionsCsvHeader(sink: Sink) {
    sink.writeCsvRow(
        "type",
        "amount",
        "subject",
        "subject_label",
        "timestamp",
        "height",
        "hash",
    )
}

internal fun appendTransactionsCsvRows(
    sink: Sink,
    transactions: List<TransactionUiState>,
) {
    transactions.forEach { transaction ->
        sink.writeCsvRow(
            transaction.type.name,
            transaction.amount.orEmpty(),
            transaction.source,
            transaction.sourceLabel.orEmpty(),
            transaction.formattedTimestamp,
            transaction.height.value.toString(),
            transaction.hash.orEmpty(),
        )
    }
}

private fun Sink.writeCsvRow(vararg values: String) {
    values.forEachIndexed { index, value ->
        if (index > 0) {
            writeString(",")
        }
        writeString("\"")
        writeString(value.replace("\"", "\"\""))
        writeString("\"")
    }
    writeString("\n")
}
