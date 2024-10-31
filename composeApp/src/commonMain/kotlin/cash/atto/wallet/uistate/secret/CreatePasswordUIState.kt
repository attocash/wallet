package cash.atto.wallet.uistate.secret

data class CreatePasswordUIState(
    val password: String?,
    val passwordConfirm: String?
) {
    companion object {
        val DEFAULT = CreatePasswordUIState(
            password = null,
            passwordConfirm = null
        )
    }
}