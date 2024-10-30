package cash.atto.wallet.uistate.secret

data class SecretPhraseUiState(
    val words: List<String>,
    val hidden: Boolean
) {
    companion object {
        val DEFAULT = SecretPhraseUiState(
            words = emptyList(),
            hidden = false
        )
    }
}