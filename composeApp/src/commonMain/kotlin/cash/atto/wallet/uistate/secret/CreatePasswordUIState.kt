package cash.atto.wallet.uistate.secret

data class CreatePasswordUIState(
    val password: String?,
    val passwordConfirm: String?,
    val checkState: PasswordCheckState
) {
    val showError get() = listOf(
        PasswordCheckState.INVALID,
        PasswordCheckState.NON_MATCHING
    ).contains(checkState)

    enum class PasswordCheckState {
        UNKNOWN,
        INVALID,
        NON_MATCHING,
        VALID;
    }

    companion object {
        val DEFAULT = CreatePasswordUIState(
            password = null,
            passwordConfirm = null,
            checkState = PasswordCheckState.UNKNOWN
        )
    }
}