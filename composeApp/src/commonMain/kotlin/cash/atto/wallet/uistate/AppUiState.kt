package cash.atto.wallet.uistate

import cash.atto.wallet.model.TermsAndConditions

data class AppUiState(
    val shownScreen: ShownScreen,
    val termsAndConditionsAccepted: Boolean = false,
    val termsAndConditionsDate: String = TermsAndConditions.EFFECTIVE_DATE,
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
