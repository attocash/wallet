package cash.atto.wallet.interactor

import cash.atto.wallet.model.PasswordStrength
import cash.atto.wallet.password.PasswordEstimator
import cash.atto.wallet.uistate.secret.CreatePasswordUIState

private val passwordEstimator by lazy { PasswordEstimator() }

class CheckPasswordInteractor {
    fun assess(password: String?): PasswordStrength {
        val value = password.orEmpty()
        return PasswordStrength(
            // The estimator bounds its work; storage/encryption still receive the entire password.
            score = if (value.isEmpty()) 0 else passwordEstimator.analyze(value).score,
            minimumLengthMet = codePointLength(value) >= PasswordStrength.MINIMUM_LENGTH,
        )
    }

    fun invoke(password: String?): CreatePasswordUIState.PasswordCheckState =
        if (assess(password).isAcceptable) {
            CreatePasswordUIState.PasswordCheckState.VALID
        } else {
            CreatePasswordUIState.PasswordCheckState.INVALID
        }

    private fun codePointLength(value: String): Int {
        var count = 0
        var index = 0
        while (index < value.length) {
            val character = value[index++]
            if (character.isHighSurrogate() && index < value.length && value[index].isLowSurrogate()) index++
            count++
        }
        return count
    }
}
