package cash.atto.wallet.password

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PasswordPatternMatchersTest {
    private val matchers = PasswordPatternMatchers(PasswordEstimatorData(emptyList(), emptyList()), referenceYear = 2026)

    @Test
    fun `dates preserve upstream day month ambiguity and remove contained matches`() {
        // Given
        val password = "2015_06_04"

        // When
        val match = matchers.dates(password).single()

        // Then
        assertEquals(password, match.token)
        assertEquals(2015, match.year)
        assertEquals(4, match.month)
        assertEquals(6, match.day)
        assertEquals("_", match.separator)
    }

    @Test
    fun `date separators follow JavaScript whitespace and ASCII digits`() {
        // Given
        val accepted = "1\u00a01\u00a091"
        val rejected = listOf("1\u00851\u008591", "٠١/٠١/٢٠٢٠")

        // When
        val acceptedMatches = matchers.dates(accepted)
        val rejectedMatches = rejected.flatMap(matchers::dates)

        // Then
        assertEquals(1991, acceptedMatches.single().year)
        assertTrue(rejectedMatches.isEmpty())
    }

    @Test
    fun `repeats select greedy or lazy base and preserve UTF16 positions`() {
        // Given
        val password = "aabaab\n😀😀\naaaaa"
        val analyzedBases = mutableListOf<String>()

        // When
        val matches =
            matchers.repeats(password) { base ->
                analyzedBases += base
                123.0
            }

        // Then
        assertEquals(listOf("aab", "😀", "a"), analyzedBases)
        assertEquals(listOf(0, 7, 12), matches.map { it.i })
        assertEquals(listOf(5, 10, 16), matches.map { it.j })
        assertEquals(listOf(2, 2, 5), matches.map { it.repeatCount })
        assertTrue(matches.all { it.baseGuesses == 123.0 })
    }

    @Test
    fun `separators preserve tie ordering and backslash regex behavior`() {
        // Given
        val tiedSeparators = "a_b-c_d-e"
        val backslashes = "a\\b\\c"

        // When
        val matches = matchers.separators(tiedSeparators)
        val backslashMatches = matchers.separators(backslashes)

        // Then
        assertEquals(listOf(1, 5), matches.map { it.i })
        assertTrue(matches.all { it.token == "_" })
        assertTrue(backslashMatches.isEmpty())
    }

    @Test
    fun `sequences use UTF16 code unit deltas and include descending skips`() {
        // Given
        val password = "😀αβγ9753"

        // When
        val matches = matchers.sequences(password)

        // Then
        assertEquals(listOf("αβγ", "9753"), matches.map { it.token })
        assertEquals(listOf(2, 5), matches.map { it.i })
        assertEquals(listOf(true, false), matches.map { it.ascending })
    }

    @Test
    fun `word sequences prefer shorter first match and preserve suffix sequences`() {
        // Given
        val password = "oneTwoThree"
        val words =
            listOf(
                dictionaryMatch(0, 2, "one", 1),
                dictionaryMatch(0, 5, "onetwo", 1),
                dictionaryMatch(3, 5, "two", 2),
                dictionaryMatch(6, 10, "three", 3),
            )

        // When
        val matches = matchers.wordSequences(password, words)

        // Then
        assertEquals(listOf(listOf("one", "two", "three"), listOf("two", "three")), matches.map { it.words })
        assertTrue(matches.all { it.ascending && it.dictionaryName == "test" })
    }

    @Test
    fun `word sequence NaN ranks preserve source ordering and are not ascending`() {
        // Given
        val password = "oneTwo"
        val words =
            listOf(
                dictionaryMatch(0, 2, "one", 1).copy(rank = Double.NaN, matchedWord = "inherited"),
                dictionaryMatch(0, 2, "one", 1),
                dictionaryMatch(3, 5, "two", 2),
            )

        // When
        val match = matchers.wordSequences(password, words).single()

        // Then
        assertEquals(listOf("inherited", "two"), match.words)
        assertEquals(false, match.ascending)
    }

    private fun dictionaryMatch(
        start: Int,
        end: Int,
        word: String,
        rank: Int,
    ) = DictionaryMatch(start, end, word, rank.toDouble(), dictionaryName = "test", matchedWord = word)
}
