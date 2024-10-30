package cash.atto.wallet.state

data class RepresentativeState(
    val wallet: String?,
    val representative: String?
) {
    companion object {
        val DEFAULT = RepresentativeState(
            wallet  = null,
            representative = null
        )
    }
}