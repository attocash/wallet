package cash.atto.wallet.password

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordEstimatorReferenceTest {
    @Test
    fun `reference inherited dictionary keys keep identical word sequence results`() {
        // Given
        val estimator = PasswordEstimator(referenceYear = 2026)

        // When
        val analysis = estimator.analyze("constructorconstructor")

        // Then
        assertEquals(0, analysis.score)
        assertEquals(3.0, analysis.guesses)
        assertTrue(analysis.sequence.single() is WordSequenceMatch)
    }

    @Test
    fun `shared estimator preserves the frozen reference scores and guess estimates`() {
        // Given
        val estimator = PasswordEstimator(referenceYear = 2026)
        val references = PasswordReferenceCases.cases
        val differences = mutableListOf<String>()

        // When
        references.forEachIndexed { index, reference ->
            val actual = estimator.analyze(reference.password)
            if (actual.score != reference.score || actual.guesses != reference.guesses) {
                differences.add(
                    "Case " + index + ": expected " + reference.score + "/" + reference.guesses +
                        ", actual " + actual.score + "/" + actual.guesses +
                        ", patterns=" + actual.sequence.joinToString { it.pattern },
                )
            }
        }

        // Then
        assertTrue(
            differences.isEmpty(),
            differences.size.toString() + " reference differences:\n" + differences.take(30).joinToString("\n"),
        )
    }
}
