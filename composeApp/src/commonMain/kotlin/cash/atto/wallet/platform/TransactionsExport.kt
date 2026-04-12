package cash.atto.wallet.platform

import cash.atto.wallet.uistate.overview.TransactionUiState
import kotlinx.io.Sink
import kotlinx.io.writeString

data class CsvExportResult(
    val location: String,
)

expect fun exportCsvFile(
    fileName: String,
    transactions: List<TransactionUiState>,
): CsvExportResult

internal fun writeTransactionsCsv(
    sink: Sink,
    transactions: List<TransactionUiState>,
) {
    sink.writeCsvRow(
        "type",
        "amount",
        "subject",
        "subject_label",
        "timestamp",
        "height",
        "hash",
    )

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

    sink.flush()
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
