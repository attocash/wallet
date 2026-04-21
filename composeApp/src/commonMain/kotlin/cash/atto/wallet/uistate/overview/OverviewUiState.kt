package cash.atto.wallet.uistate.overview

import cash.atto.commons.AttoAccountEntry
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoUnit
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class OverviewUiState(
    private val balance: BigDecimal?,
    val priceUsd: BigDecimal? = null,
    val apy: BigDecimal? = null,
    val transactionListUiState: TransactionListUiState = TransactionListUiState.DEFAULT,
    val receiveAddress: String?,
    val pendingReceivableCount: Int = 0,
    val pendingReceivableAmount: BigDecimal = BigDecimal.ZERO,
    val voterName: String? = null,
) {
    val headerUiState
        get() =
            OverviewHeaderUiState(
                attoCoins = balance,
            )

    companion object {
        val DEFAULT =
            OverviewUiState(
                balance = null,
                priceUsd = null,
                transactionListUiState = TransactionListUiState.DEFAULT,
                receiveAddress = null,
            )

        suspend fun empty() =
            OverviewUiState(
                balance = null,
                priceUsd = null,
                transactionListUiState =
                    TransactionListUiState(
                        transactions = emptyList(),
                        showHint = true,
                    ),
                receiveAddress = null,
            )
    }
}

@OptIn(ExperimentalTime::class)
fun buildTransactionListUiState(
    entries: List<AttoAccountEntry>,
    addressLabelResolver: (String) -> String? = { null },
    voterLabelResolver: (String) -> String? = { null },
): TransactionListUiState =
    TransactionListUiState(
        transactions =
            entries.mapNotNull { entry ->
                entry.toTransactionUiState(
                    addressLabelResolver = addressLabelResolver,
                    voterLabelResolver = voterLabelResolver,
                )
            },
        showHint = entries.isEmpty(),
    )

@OptIn(ExperimentalTime::class)
private fun AttoAccountEntry.toTransactionUiState(
    addressLabelResolver: (String) -> String?,
    voterLabelResolver: (String) -> String?,
): TransactionUiState? {
    val subjectAddress =
        AttoAddress(
            subjectAlgorithm,
            subjectPublicKey,
        ).toString()

    return when (blockType) {
        AttoBlockType.SEND -> {
            TransactionUiState(
                type = TransactionType.SEND,
                amount = "- ${amount().toString(AttoUnit.ATTO)}",
                source = subjectAddress,
                sourceLabel = addressLabelResolver(subjectAddress),
                timestamp = Instant.fromEpochMilliseconds(timestamp.toEpochMilliseconds()),
                height = height,
                hash = hash.toString(),
            )
        }

        AttoBlockType.RECEIVE -> {
            TransactionUiState(
                type = TransactionType.RECEIVE,
                amount = "+ ${amount().toString(AttoUnit.ATTO)}",
                source = subjectAddress,
                sourceLabel = addressLabelResolver(subjectAddress),
                timestamp = Instant.fromEpochMilliseconds(timestamp.toEpochMilliseconds()),
                height = height,
                hash = hash.toString(),
            )
        }

        AttoBlockType.OPEN -> {
            TransactionUiState(
                type = TransactionType.OPEN,
                amount = "+ ${amount().toString(AttoUnit.ATTO)}",
                source = subjectAddress,
                sourceLabel = addressLabelResolver(subjectAddress),
                timestamp = Instant.fromEpochMilliseconds(timestamp.toEpochMilliseconds()),
                height = height,
                hash = hash.toString(),
            )
        }

        AttoBlockType.CHANGE -> {
            TransactionUiState(
                type = TransactionType.CHANGE,
                amount = null,
                source = subjectAddress,
                sourceLabel = voterLabelResolver(subjectAddress),
                timestamp = Instant.fromEpochMilliseconds(timestamp.toEpochMilliseconds()),
                height = height,
                hash = hash.toString(),
            )
        }

        else -> {
            null
        }
    }
}

private fun AttoAccountEntry.amount() =
    when (blockType) {
        AttoBlockType.SEND -> previousBalance - balance
        else -> balance - previousBalance
    }
