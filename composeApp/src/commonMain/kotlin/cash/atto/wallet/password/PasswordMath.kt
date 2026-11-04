package cash.atto.wallet.password

import kotlin.math.pow

// V8 and platform libm can round the same integer power to different doubles. The fixed estimator's
// complete base/exponent domain is frozen from its reference engine and checked by PasswordMathTest.
internal object PasswordMath {
    fun pow(
        base: Double,
        exponent: Int,
    ): Double {
        val referencePowers = ReferencePowerData.values[base]
        // Custom keyboard graphs are only used by isolated matcher tests, outside the bundled configuration.
        return referencePowers?.get(exponent) ?: base.pow(exponent)
    }
}
