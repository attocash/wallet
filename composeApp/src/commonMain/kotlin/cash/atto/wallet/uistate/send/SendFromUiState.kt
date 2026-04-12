package cash.atto.wallet.uistate.send

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class SendFromUiState(
    val accountName: String?,
    val accountSeed: String?,
    val accountBalance: String?,
    val accountBalanceUsd: BigDecimal?,
    val amountString: String?,
    val amountUsd: BigDecimal?,
    val address: String?,
    val showAmountError: Boolean,
    val showAddressError: Boolean,
    val showLoader: Boolean,
    val isUsdMode: Boolean = false,
    val equivalentDisplay: String = "",
    val priceUsd: BigDecimal? = null,
) {
    companion object {
        val DEFAULT =
            SendFromUiState(
                accountName = null,
                accountSeed = null,
                accountBalance = null,
                accountBalanceUsd = null,
                amountString = null,
                amountUsd = null,
                address = null,
                showAmountError = false,
                showAddressError = false,
                showLoader = true,
            )
    }
}
