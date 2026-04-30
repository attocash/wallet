package cash.atto.wallet.repository

import cash.atto.commons.AttoAccount
import cash.atto.commons.AttoAddress
import cash.atto.commons.AttoKeyIndex
import cash.atto.wallet.model.AccountPreferenceStatus

data class WalletAccountState(
    val index: AttoKeyIndex,
    val address: AttoAddress,
    val account: AttoAccount?,
    val status: AccountPreferenceStatus,
) {
    val isActive: Boolean
        get() = status == AccountPreferenceStatus.ACTIVATED
}
