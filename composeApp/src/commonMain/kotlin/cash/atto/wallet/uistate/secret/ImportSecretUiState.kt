package cash.atto.wallet.uistate.secret

data class ImportSecretUiState(
    val input: String?
) {
    companion object {
        val DEFAULT = ImportSecretUiState(null)
    }
}