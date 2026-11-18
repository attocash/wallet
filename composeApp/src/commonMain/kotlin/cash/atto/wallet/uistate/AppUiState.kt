package cash.atto.wallet.uistate

import cash.atto.wallet.model.TermsAndConditions

data class AppUiState(
    val shownScreen: ShownScreen,
    val authenticationError: String? = null,
    val termsAndConditionsAccepted: Boolean = false,
    val termsAndConditionsDate: String = TermsAndConditions.EFFECTIVE_DATE,
    val unlockingWallet: Boolean = false,
) {
    enum class ShownScreen {
        LOADER,
        OVERVIEW,
        PASSWORD_CREATE,
        PASSWORD_ENTER,
        WELCOME,
    }

    companion object {
        val DEFAULT = AppUiState(ShownScreen.LOADER)
    }
}
