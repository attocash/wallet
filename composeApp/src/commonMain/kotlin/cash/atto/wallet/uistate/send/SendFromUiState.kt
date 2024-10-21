package cash.atto.wallet.uistate.send

import java.math.BigDecimal

data class SendFromUiState(
    val accountName: String?,
    val accountSeed: String?,
    val accountBalance: BigDecimal,
    val amount: BigDecimal?,
    val address: String?
) {
    companion object {
        val DEFAULT = SendFromUiState(
            accountName = null,
            accountSeed = null,
            accountBalance = BigDecimal.ZERO,
            amount = null,
            address = null
        )
    }
}