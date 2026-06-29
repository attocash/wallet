package cash.atto.wallet.repository

import cash.atto.commons.AttoSendBlock

data class WalletSendResult(
    val block: AttoSendBlock,
    val publishMs: Long?,
)
