package cash.atto.wallet.model

data class PasswordStrength(
    val score: Int,
    val characterCount: Int,
) {
    val minimumLengthMet: Boolean
        get() = characterCount >= MINIMUM_LENGTH

    val isAcceptable: Boolean
        get() = minimumLengthMet && score >= MINIMUM_SCORE

    companion object {
        const val MINIMUM_LENGTH = 8
        const val MINIMUM_SCORE = 2
    }
}
