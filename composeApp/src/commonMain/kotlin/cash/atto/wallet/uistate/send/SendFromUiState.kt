package cash.atto.wallet.uistate.send

import com.ionspin.kotlin.bignum.decimal.BigDecimal

data class SendFromUiState(
    val accountName: String?,
    val accountSeed: String?,
    val accountBalance: BigDecimal,
    val amountString: String?,
    val address: String?,
    val showAmountError: Boolean,
    val showAddressError: Boolean,
    val showLoader: Boolean
) {
    companion object {
        val DEFAULT = SendFromUiState(
            accountName = null,
            accountSeed = null,
            accountBalance = BigDecimal.ZERO,
            amountString = null,
            address = null,
            showAmountError = false,
            showAddressError = false,
            showLoader = true
        )
    }
}