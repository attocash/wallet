package cash.atto.wallet.uistate.secret

data class ImportSecretUiState(
    val input: String?,
    val errorMessage: String?
) {
    val inputValid get() = (errorMessage == null)

    companion object {
        val DEFAULT = ImportSecretUiState(
            input = null,
            errorMessage = "Input is empty"
        )
    }
}