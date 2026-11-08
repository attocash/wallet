package cash.atto.wallet.platform

import cash.atto.commons.AttoPublicKey
import cash.atto.wallet.repository.PersistentAccountEntryRepository
import cash.atto.wallet.uistate.overview.TransactionType
import cash.atto.wallet.uistate.overview.buildTransactionListUiState
import kotlinx.io.Sink

internal suspend fun PersistentAccountEntryRepository.exportCsv(
    publicKey: AttoPublicKey,
    selectedTypes: Set<TransactionType>,
    addressLabelResolver: (String) -> String? = { null },
    voterLabelResolver: (String) -> String? = { null },
    hashLabelResolver: (String) -> String? = { null },
    sink: Sink,
) {
    writeTransactionsCsvHeader(sink)

    var beforeHeightExclusive: Long? = null

    while (true) {
        val page =
            if (beforeHeightExclusive == null) {
                listRecent(publicKey, EXPORT_PAGE_SIZE)
            } else {
                listBefore(
                    publicKey = publicKey,
                    beforeHeightExclusive = beforeHeightExclusive,
                    limit = EXPORT_PAGE_SIZE,
                )
            }

        if (page.isEmpty()) {
            sink.flush()
            return
        }

        val transactions =
            buildTransactionListUiState(
                entries = page,
                addressLabelResolver = addressLabelResolver,
                voterLabelResolver = voterLabelResolver,
                hashLabelResolver = hashLabelResolver,
            ).transactions
                .filterNotNull()
                .filter { it.type in selectedTypes }

        appendTransactionsCsvRows(sink, transactions)

        if (page.size < EXPORT_PAGE_SIZE) {
            sink.flush()
            return
        }

        beforeHeightExclusive =
            page
                .lastOrNull()
                ?.height
                ?.value
                ?.toLong()
    }
}

private const val EXPORT_PAGE_SIZE = 500
