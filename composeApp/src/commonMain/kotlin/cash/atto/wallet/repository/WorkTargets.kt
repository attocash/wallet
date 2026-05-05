package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoPublicKey
import cash.atto.commons.AttoWorkTarget

internal fun nextWorkTarget(
    account: AttoAccount?,
    publicKey: AttoPublicKey,
): AttoWorkTarget =
    AttoWorkTarget(
        account
            ?.lastTransactionHash
            ?.value
            ?: publicKey.value,
    )
