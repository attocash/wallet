package cash.atto.wallet.password

internal data class PasswordAnalysis(
    val guesses: Double,
    val score: Int,
    val sequence: List<PasswordMatch>,
)
