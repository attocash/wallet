package cash.atto.wallet.uistate.settings

data class RepresentativeUIState(
    val representative: String?,
    val showError: Boolean = false
) {
    companion object {
        val DEFAULT = RepresentativeUIState(null)
    }
}