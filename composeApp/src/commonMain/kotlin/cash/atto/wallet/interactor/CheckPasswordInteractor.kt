package cash.atto.wallet.interactor

import cash.atto.wallet.uistate.secret.CreatePasswordUIState

class CheckPasswordInteractor {
    fun invoke(password: String?) : CreatePasswordUIState.PasswordCheckState =
        when {
            password == null ->
                cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.INVALID

            password.length < 6 ->
                cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.INVALID

            !password.any { it.isUpperCase() } ->
                cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.INVALID

            !password.any { it.isLowerCase() } ->
                cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.INVALID

            !password.any { it.isDigit() } ->
                cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.INVALID

            !password.any { !it.isLetterOrDigit() } ->
                cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.INVALID

            else -> cash.atto.wallet.uistate.secret.CreatePasswordUIState.PasswordCheckState.VALID
        }
}