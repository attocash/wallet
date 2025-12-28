package cash.atto.wallet.uistate.overview

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoAmount
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoUnit
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class OverviewUiState(
    private val balance: BigDecimal?,
    val entries: List<AttoAccountEntry?>,
    val receiveAddress: String?
) {
    val headerUiState
        get() = OverviewHeaderUiState(
            attoCoins = balance
        )

    @OptIn(ExperimentalTime::class)
    val transactionListUiState
        get() = TransactionListUiState(
            transactions = entries
                .filterNotNull()
                .sortedByDescending { it.height }
                .mapNotNull {
                    when (it.blockType) {
                        AttoBlockType.SEND -> TransactionUiState(
                            type = TransactionType.SEND,
                            amount = "- " + it.amount().toString(AttoUnit.ATTO),
                            source = AttoAddress(
                                it.subjectAlgorithm,
                                it.subjectPublicKey
                            ).toString(),
                            timestamp = Instant.fromEpochMilliseconds(it.timestamp.toEpochMilliseconds()),
                            height = it.height
                        )

                        AttoBlockType.RECEIVE,
                        AttoBlockType.OPEN -> TransactionUiState(
                            type = TransactionType.RECEIVE,
                            amount = "+ " + it.amount().toString(AttoUnit.ATTO),
                            source = AttoAddress(
                                it.subjectAlgorithm,
                                it.subjectPublicKey
                            ).toString(),
                            timestamp = Instant.fromEpochMilliseconds(it.timestamp.toEpochMilliseconds()),
                            height = it.height
                        )

                        AttoBlockType.CHANGE -> TransactionUiState(
                            type = TransactionType.CHANGE,
                            amount = null,
                            source = AttoAddress(
                                it.subjectAlgorithm,
                                it.subjectPublicKey
                            ).toString(),
                            timestamp = Instant.fromEpochMilliseconds(it.timestamp.toEpochMilliseconds()),
                            height = it.height
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

    private fun AttoAccountEntry.amount(): AttoAmount {
        return when (this.blockType) {
            AttoBlockType.SEND -> this.previousBalance - this.balance
            else -> this.balance - this.previousBalance
        }
    }
}