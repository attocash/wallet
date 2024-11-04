package cash.atto.wallet.uistate.overview

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoOpenBlock
import cash.atto.commons.AttoReceiveBlock
import cash.atto.commons.AttoSendBlock
import cash.atto.commons.AttoUnit
import java.math.BigDecimal

data class OverviewUiState(
    private val balance: BigDecimal?,
    val entries: List<AttoAccountEntry?>,
    val receiveAddress: String?
) {
    val headerUiState
        get() = OverviewHeaderUiState(
            attoCoins = balance
        )

    val transactionListUiState
        get() = TransactionListUiState(
            transactions = entries
                .filterNotNull()
                .map {
                    when (it.blockType) {
                         AttoBlockType.SEND -> TransactionUiState(
                            type = TransactionType.SEND,
                            amount = it.amount.toString(AttoUnit.ATTO),
                            source = AttoAddress(it.subjectAlgorithm, it.subjectPublicKey).toString(),
                        )

                        AttoBlockType.RECEIVE -> TransactionUiState(
                            type = TransactionType.RECEIVE,
                            amount = it.amount.toString(AttoUnit.ATTO),
                            source = AttoAddress(it.subjectAlgorithm, it.subjectPublicKey).toString(),
                        )

                        AttoBlockType.OPEN -> TransactionUiState(
                            type = TransactionType.RECEIVE,
                            amount = it.amount.toString(AttoUnit.ATTO),
                            source = AttoAddress(it.subjectAlgorithm, it.subjectPublicKey).toString(),
                        )

                        AttoBlockType.CHANGE -> TransactionUiState(
                            type = TransactionType.CHANGE,
                            amount = it.amount.toString(AttoUnit.ATTO),
                            source = AttoAddress(it.subjectAlgorithm, it.subjectPublicKey).toString(),
                        )

                        else -> null
                    }
                },
            showHint = entries.isEmpty()
        )

    companion object {
        val DEFAULT = OverviewUiState(
            balance = null,
            entries = List<AttoAccountEntry?>(2) { null },
            receiveAddress = null
        )

        suspend fun empty() = OverviewUiState(
            balance = null,
            entries = emptyList(),
            receiveAddress = null
        )
    }
}