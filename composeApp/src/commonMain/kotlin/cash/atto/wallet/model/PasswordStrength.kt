package cash.atto.wallet.model

data class PasswordStrength(
    val score: Int,
    val minimumLengthMet: Boolean,
) {
    val isAcceptable: Boolean
        get() = minimumLengthMet && score >= MINIMUM_SCORE

    companion object {
        const val MINIMUM_LENGTH = 15
        const val MINIMUM_SCORE = 2
    }
}
