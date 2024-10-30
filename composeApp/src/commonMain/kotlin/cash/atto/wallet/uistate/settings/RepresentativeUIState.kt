package cash.atto.wallet.uistate.settings

data class RepresentativeUIState(
    val representative: String?
) {
    companion object {
        val DEFAULT = RepresentativeUIState(null)
    }
}