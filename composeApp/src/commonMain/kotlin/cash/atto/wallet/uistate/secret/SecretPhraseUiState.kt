package cash.atto.wallet.uistate.secret

data class SecretPhraseUiState(
    val words: List<String>
) {
    companion object {
        val DEFAULT = SecretPhraseUiState(emptyList())
    }
}