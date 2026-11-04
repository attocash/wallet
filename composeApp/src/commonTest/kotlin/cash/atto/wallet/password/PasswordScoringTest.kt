package cash.atto.wallet.password

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordScoringTest {
    private val scoring = PasswordScoring(PasswordEstimatorData(emptyList(), emptyList()), referenceYear = 2026)

    @Test
    fun `empty password has one guess and no match sequence`() {
        // Given
        val password = ""

        // When
        val analysis = scoring.analyze(password, emptyList())

        // Then
        assertEquals(PasswordAnalysis(1.0, 0, emptyList()), analysis)
    }

    @Test
    fun `dynamic search combines dictionary submatches with sequence penalties`() {
        // Given
        val first = DictionaryMatch(0, 2, "abc", rank = 2.0)
        val second = DictionaryMatch(3, 5, "DEF", rank = 3.0)

        // When
        val analysis = scoring.analyze("abcDEF", listOf(second, first))
        val withoutAdditive = scoring.analyze("abcDEF", listOf(second, first), excludeAdditive = true)

        // Then: reference @zxcvbn-ts/core 4.2.0 uses 2! * 50 * 50 + 10000.
        assertEquals(15_000.0, analysis.guesses)
        assertEquals(listOf(first, second), analysis.sequence)
        assertEquals(1, analysis.score)
        assertEquals(5_000.0, withoutAdditive.guesses)
    }

    @Test
    fun `whole dictionary match beats overlapping submatches`() {
        // Given
        val password = "abcDEF"
        val whole = DictionaryMatch(0, 5, password, rank = 100.0)
        val matches =
            listOf(
                DictionaryMatch(0, 2, "abc", rank = 2.0),
                whole,
                DictionaryMatch(3, 5, "DEF", rank = 3.0),
            )

        // When
        val analysis = scoring.analyze(password, matches)

        // Then: uncommon mixed capitalization contributes 41 variations.
        assertEquals(4_101.0, analysis.guesses)
        assertEquals(listOf(whole), analysis.sequence)
    }

    @Test
    fun `capitalization follows upstream Latin character classes`() {
        // Given
        val referenceGuesses = mapOf("Ätto" to 200.0, "päSSwörd" to 3_600.0, "aßA" to 1_000.0, "ß" to 100.0)

        // When
        val guesses =
            referenceGuesses.keys.associateWith { token ->
                scoring.estimateGuesses(DictionaryMatch(0, token.lastIndex, token, rank = 100.0), token)
            }

        // Then: these values were captured from the installed 4.2.0 reference implementation.
        assertEquals(referenceGuesses, guesses)
    }

    @Test
    fun `each recorded l33t substitution contributes its own variation factor`() {
        // Given
        val match =
            DictionaryMatch(
                0,
                4,
                "a4a4a",
                rank = 10.0,
                l33t = true,
                substitutions = listOf(PasswordSubstitution(1, "a", "4"), PasswordSubstitution(3, "a", "4")),
            )

        // When
        val guesses = scoring.estimateGuesses(match, match.token)

        // Then: each substitution contributes C(5,1) + C(5,2), including repeated records.
        assertEquals(2_250.0, guesses)
    }

    @Test
    fun `diceware dictionary uses fixed entropy despite spelling variations`() {
        // Given
        val match = DictionaryMatch(0, 5, "D1C3S!", dictionaryName = "diceware", rank = 1.0, reversed = true, l33t = true)

        // When
        val guesses = scoring.estimateGuesses(match, match.token)

        // Then
        assertEquals(3_888.0, guesses)
    }

    @Test
    fun `spatial scoring includes average degree turns and shifted keys`() {
        // Given
        val graph = PasswordKeyboardGraph("tiny", mapOf("a" to listOf("b", null), "b" to listOf("a", "c"), "c" to listOf("b")))
        val spatialScoring = PasswordScoring(PasswordEstimatorData(emptyList(), listOf(graph)), referenceYear = 2026)
        val match = SpatialMatch(0, 2, "abc", graph = "tiny", turns = 2, shiftedCount = 1)

        // When
        val guesses = spatialScoring.estimateGuesses(match, match.token)

        // Then: the installed reference rounds its 18 2/3 base guesses times three shift variations to 56.
        assertEquals(56.0, guesses)
    }

    @Test
    fun `pattern estimates preserve reference costs and submatch minimums`() {
        // Given
        val fixtures =
            listOf(
                BruteForceMatch(0, 0, "x") to 11.0,
                DateMatch(0, 9, "2026-01-01", year = 2026, month = 1, day = 1, separator = "-") to 30_660.0,
                RegexMatch(0, 3, "1926", regexName = PasswordRegex.RECENT_YEAR) to 100.0,
                RegexMatch(0, 2, "123", regexName = PasswordRegex.DIGITS) to 1_000.0,
                RepeatMatch(0, 2, "aaa", baseToken = "a", baseGuesses = 11.0, repeatCount = 3) to 33.0,
                SequenceMatch(0, 2, "zyx", ascending = false) to 24.0,
                SequenceMatch(0, 2, "234") to 30.0,
                SequenceMatch(0, 2, "bcd") to 78.0,
                SeparatorMatch(0, 0, " ") to 10.0,
                WordSequenceMatch(
                    0,
                    17,
                    "one two three four",
                    words = listOf("one", "two", "three", "four"),
                    dictionaryName = "cardinalNumbers-en",
                    ascending = true,
                ) to 96.0,
            )

        // When
        val actual = fixtures.map { (match, _) -> scoring.estimateGuesses(match, match.token) }
        val submatch = scoring.estimateGuesses(RepeatMatch(0, 2, "aaa", baseToken = "a", baseGuesses = 11.0, repeatCount = 3), "aaab")

        // Then
        assertEquals(fixtures.map { it.second }, actual)
        assertEquals(50.0, submatch)
    }

    @Test
    fun `score thresholds include the upstream five guess delta`() {
        // Given
        val thresholds = listOf(1e3 to 0, 1e6 to 1, 1e8 to 2, 1e10 to 3)

        // When
        val scores =
            thresholds.map { (threshold, _) ->
                scoring.scoreFromGuesses(threshold + 4) to scoring.scoreFromGuesses(threshold + 5)
            }

        // Then: the next score starts exactly five guesses above each nominal threshold.
        assertEquals(thresholds.map { (_, score) -> score to score + 1 }, scores)
    }

    @Test
    fun `non numeric inherited dictionary rank preserves reference NaN comparisons`() {
        // Given
        val password = "constructor"
        val match = DictionaryMatch(0, password.lastIndex, password, rank = Double.NaN)

        // When
        val matchGuesses = scoring.estimateGuesses(match, password)
        val analysis = scoring.analyze(password, listOf(match))

        // Then: a NaN estimate cannot suppress the finite brute-force candidate.
        assertTrue(matchGuesses.isNaN())
        assertEquals(100_000_000_001.0, analysis.guesses)
        assertEquals(listOf(BruteForceMatch(0, password.lastIndex, password)), analysis.sequence)
    }

    @Test
    fun `maximum supported password length keeps a finite brute force estimate`() {
        // Given
        val password = "x".repeat(256)

        // When
        val analysis = scoring.analyze(password, emptyList())

        // Then
        assertEquals(4, analysis.score)
        assertTrue(analysis.guesses.isFinite())
        assertEquals(listOf(BruteForceMatch(0, 255, password)), analysis.sequence)
    }
}
