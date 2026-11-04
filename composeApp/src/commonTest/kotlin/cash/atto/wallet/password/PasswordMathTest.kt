package cash.atto.wallet.password

import kotlin.test.Test
import kotlin.test.assertEquals

class PasswordMathTest {
    @Test
    fun `all supported scoring powers preserve exact frozen reference bits`() {
        // Given: reference engine Node 24.18.0, V8 13.6.233.17-node.50, in generator order.
        val bases = listOf(2.0, 6.0, 10.0, 26.0, 33.0, 52.0, 62.0, 10_000.0, 440.0 / 95, 432.0 / 94, 76.0 / 15, 84.0 / 16, 444.0 / 96)
        var fingerprint = 0xcbf29ce484222325UL

        // When: hash all 3341 IEEE-754 bit patterns with the same wordwise reference fingerprint.
        for (base in bases) {
            for (exponent in 0..256) {
                fingerprint = (fingerprint xor PasswordMath.pow(base, exponent).toBits().toULong()) * 0x100000001b3UL
            }
        }

        // Then
        assertEquals(0x9534f99605ee7286UL, fingerprint)
    }

    @Test
    fun `bundled graphs and every scoring base use reference tables for the complete input domain`() {
        // Given
        val scoringBases = setOf(2.0, 6.0, 10.0, 26.0, 33.0, 52.0, 62.0, 10_000.0)
        val graphBases =
            BundledPasswordData.data.graphs.map { graph ->
                graph.adjacency.values
                    .sumOf { neighbors -> neighbors.count { !it.isNullOrEmpty() } }
                    .toDouble() / graph.adjacency.size
            }

        // When
        val requiredBases = scoringBases + graphBases
        val availablePowers = ReferencePowerData.values.mapValues { (_, powers) -> powers.size }

        // Then: the fallback for synthetic test graphs cannot be reached by the bundled configuration.
        assertEquals(requiredBases, availablePowers.keys)
        assertEquals(requiredBases.associateWith { 257 }, availablePowers)
    }

    @Test
    fun `power boundaries preserve V8 rounding finite limits and overflow`() {
        // Given
        val fixtures =
            listOf(
                Triple(10.0, 0, 1.0),
                Triple(10.0, 22, 1e22),
                Triple(10.0, 23, 1.0000000000000001e23),
                Triple(10.0, 24, 1e24),
                Triple(10.0, 256, 1e256),
                Triple(10_000.0, 77, 1e308),
                Triple(10_000.0, 78, Double.POSITIVE_INFINITY),
                Triple(10_000.0, 256, Double.POSITIVE_INFINITY),
                Triple(2.0, 256, 1.157920892373162e77),
                Triple(6.0, 5, 7_776.0),
            )

        // When
        val actualBits = fixtures.map { (base, exponent, _) -> PasswordMath.pow(base, exponent).toBits() }

        // Then
        assertEquals(fixtures.map { (_, _, expected) -> expected.toBits() }, actualBits)
    }
}
