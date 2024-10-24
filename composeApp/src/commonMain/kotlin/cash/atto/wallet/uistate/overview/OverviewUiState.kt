package cash.atto.wallet.uistate.overview

import cash.atto.commons.AttoBlockType
import cash.atto.commons.AttoOpenBlock
import cash.atto.commons.AttoReceiveBlock
import cash.atto.commons.AttoSendBlock
import cash.atto.commons.AttoTransaction
import cash.atto.commons.AttoUnit
import java.math.BigDecimal

data class OverviewUiState(
    private val balance: BigDecimal?,
    val transactions: List<AttoTransaction?>,
    val receiveAddress: String?
) {
    val headerUiState get() = OverviewHeaderUiState(
        attoCoins = balance
    )

    val transactionListUiState get() = TransactionListUiState(
        transactions = transactions
            .filterNotNull()
            .map {
                when (it.block) {
                    is AttoSendBlock -> TransactionUiState(
                        type = TransactionType.SEND,
                        amount = (it.block as AttoSendBlock)
                            .amount
                            .toString(AttoUnit.ATTO),
                        source = (it.block as AttoSendBlock)
                            .receiverPublicKey
                            .toString()
                    )

                    is AttoReceiveBlock -> TransactionUiState(
                        type = TransactionType.RECEIVE,
                        amount = "Unknown amount",
                        source = "Unknown Source"
//                        amount = (it.block as AttoReceiveBlock).amount.toString(AttoUnit.ATTO),
//                        source = it.block.receiverPublicKey.toString()
                    )

                    is AttoOpenBlock -> TransactionUiState(
                        type = TransactionType.RECEIVE,
                        amount = "Unknown amount",
                        source = "Unknown Source"
//                        amount = (it.block as AttoOpenBlock).balance.toString(AttoUnit.ATTO),
//                        source = (it.block as AttoOpenBlock)
//                            .representativePublicKey
//                            .toString()
                    )

                    else -> null
                }
            },
        showHint = transactions.isEmpty()
    )

    companion object {
        val DEFAULT = OverviewUiState(
            balance = null,
            transactions = List<AttoTransaction?>(2) { null },
            receiveAddress = null
        )

        suspend fun empty() = OverviewUiState(
            balance = null,
            transactions = emptyList(),
            receiveAddress = null
        )
    }
}