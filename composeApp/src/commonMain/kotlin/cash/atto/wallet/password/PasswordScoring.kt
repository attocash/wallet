package cash.atto.wallet.password

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

// Ported from @zxcvbn-ts/core 4.2.0 scoring and matcher scoring.
internal class PasswordScoring(
    data: PasswordEstimatorData,
    private val referenceYear: Int,
) {
    private val graphStatistics =
        data.graphs.associate { graph ->
            val positions = graph.adjacency.size
            val neighbors = graph.adjacency.values.sumOf { entries -> entries.count { !it.isNullOrEmpty() } }
            graph.name to GraphStatistics(positions, neighbors.toDouble() / positions)
        }

    fun analyze(
        password: String,
        matches: List<PasswordMatch>,
        excludeAdditive: Boolean = false,
    ): PasswordAnalysis {
        if (password.isEmpty()) return PasswordAnalysis(1.0, 0, emptyList())

        // Repeat matching recursively analyzes its base token, so each analysis owns its complete search state.
        val bestMatches = List(password.length) { mutableMapOf<Int, PasswordMatch>() }
        val guessesProducts = List(password.length) { mutableMapOf<Int, Double>() }
        val totalGuesses = List(password.length) { mutableMapOf<Int, Double>() }
        val guessesCache = mutableMapOf<PasswordMatch, Double>()
        val matchesByEnd = List(password.length) { mutableListOf<PasswordMatch>() }
        matches.forEach { matchesByEnd[it.j].add(it) }
        matchesByEnd.forEach { it.sortBy(PasswordMatch::i) }

        fun update(
            match: PasswordMatch,
            sequenceLength: Int,
        ) {
            var product =
                if (match is BruteForceMatch) {
                    estimateGuesses(match, password)
                } else {
                    guessesCache.getOrPut(match) { estimateGuesses(match, password) }
                }
            if (sequenceLength > 1) product *= guessesProducts[match.i - 1].getValue(sequenceLength - 1)
            var guesses = factorial(sequenceLength) * product
            if (!excludeAdditive) guesses += PasswordMath.pow(10_000.0, sequenceLength - 1)
            if (totalGuesses[match.j].any { (length, previous) -> length <= sequenceLength && previous <= guesses }) return
            bestMatches[match.j][sequenceLength] = match
            guessesProducts[match.j][sequenceLength] = product
            totalGuesses[match.j][sequenceLength] = guesses
        }

        fun bruteForceMatch(
            start: Int,
            end: Int,
        ) = BruteForceMatch(start, end, password.substring(start, end + 1))

        for (end in password.indices) {
            for (match in matchesByEnd[end]) {
                if (match.i == 0) {
                    update(match, 1)
                } else {
                    for (length in bestMatches[match.i - 1].keys.sorted()) update(match, length + 1)
                }
            }
            update(bruteForceMatch(0, end), 1)
            for (start in 1..end) {
                val match = bruteForceMatch(start, end)
                for (length in bestMatches[start - 1].keys.sorted()) {
                    if (bestMatches[start - 1].getValue(length) !is BruteForceMatch) update(match, length + 1)
                }
            }
        }

        var sequenceLength = 0
        var guesses = Double.POSITIVE_INFINITY
        for (length in totalGuesses.last().keys.sorted()) {
            val candidate = totalGuesses.last().getValue(length)
            if (candidate < guesses) {
                sequenceLength = length
                guesses = candidate
            }
        }
        val sequence = mutableListOf<PasswordMatch>()
        var end = password.lastIndex
        while (end >= 0) {
            val match = bestMatches[end].getValue(sequenceLength)
            sequence.add(match)
            end = match.i - 1
            sequenceLength--
        }
        sequence.reverse()
        return PasswordAnalysis(guesses, scoreFromGuesses(guesses), sequence)
    }

    fun estimateGuesses(
        match: PasswordMatch,
        password: String,
    ): Double {
        val minimum =
            if (match.token.length < password.length) {
                if (match.token.length == 1) 10.0 else 50.0
            } else {
                1.0
            }
        val guesses =
            when (match) {
                is BruteForceMatch -> bruteForceGuesses(match.token.length)
                is DictionaryMatch -> dictionaryGuesses(match)
                is DateMatch -> max(abs(match.year.toDouble() - referenceYear), 21.0) * 365 * if (match.separator.isEmpty()) 1 else 4
                is RegexMatch -> regexGuesses(match)
                is RepeatMatch -> match.baseGuesses * match.repeatCount
                is SequenceMatch -> sequenceGuesses(match)
                is SpatialMatch -> spatialGuesses(match)
                is SeparatorMatch -> 10.0
                is WordSequenceMatch -> wordSequenceFactorial(match.words.size) * PasswordMath.pow(2.0, match.words.size - 2)
            }
        return max(guesses, minimum)
    }

    private fun bruteForceGuesses(length: Int): Double {
        val guesses = PasswordMath.pow(10.0, length).let { if (it == Double.POSITIVE_INFINITY) Double.MAX_VALUE else it }
        return max(guesses, if (length == 1) 11.0 else 51.0)
    }

    private fun dictionaryGuesses(match: DictionaryMatch): Double {
        if (match.dictionaryName == "diceware") return PasswordMath.pow(6.0, 5) / 2
        return match.rank * uppercaseVariations(match.token) * l33tVariations(match) * if (match.reversed) 2 else 1
    }

    private fun uppercaseVariations(word: String): Double {
        // The upstream /i regex accepts these exact Latin-1 ranges, including its unusual upper/lower overlap at sharp s.
        val cleaned =
            word.filter { char ->
                char in 'A'..'Z' ||
                    char in 'a'..'z' ||
                    char in '\u00bf'..'\u00df' ||
                    char in '\u00e0'..'\u00f6' ||
                    char in '\u00f8'..'\u00fe'
            }
        if (cleaned.none(::isUppercase) || cleaned.lowercase() == cleaned) return 1.0
        val capitalized = cleaned.length > 1 && isUppercase(cleaned.first()) && cleaned.drop(1).none(::isUppercase)
        val endCapitalized = cleaned.length > 1 && isUppercase(cleaned.last()) && cleaned.dropLast(1).none(::isUppercase)
        if (capitalized || endCapitalized || cleaned.none(::isLowercase)) return 2.0
        return variations(cleaned.count(::isUppercase), cleaned.count(::isLowercase))
    }

    private fun isUppercase(char: Char) = char in 'A'..'Z' || char in '\u00bf'..'\u00df'

    private fun isLowercase(char: Char) = char in 'a'..'z' || char in '\u00df'..'\u00ff'

    private fun l33tVariations(match: DictionaryMatch): Double {
        if (!match.l33t) return 1.0
        val lowercase = match.token.lowercase()
        var result = 1.0
        for (substitution in match.substitutions) {
            val substituted = countSubstring(lowercase, substitution.substitution)
            val unsubstituted = countSubstring(lowercase, substitution.letter)
            result *= if (substituted == 0 || unsubstituted == 0) 2.0 else variations(substituted, unsubstituted)
        }
        return result
    }

    private fun countSubstring(
        value: String,
        substring: String,
    ): Int {
        var count = 0
        var position = value.indexOf(substring)
        while (position >= 0) {
            count++
            position = value.indexOf(substring, position + substring.length)
        }
        return count
    }

    private fun regexGuesses(match: RegexMatch): Double =
        when (match.regexName) {
            PasswordRegex.ALPHA_LOWER, PasswordRegex.ALPHA_UPPER -> PasswordMath.pow(26.0, match.token.length)
            PasswordRegex.ALPHA -> PasswordMath.pow(52.0, match.token.length)
            PasswordRegex.ALPHANUMERIC -> PasswordMath.pow(62.0, match.token.length)
            PasswordRegex.DIGITS -> PasswordMath.pow(10.0, match.token.length)
            PasswordRegex.SYMBOLS -> PasswordMath.pow(33.0, match.token.length)
            PasswordRegex.RECENT_YEAR -> max(abs(match.token.toDouble() - referenceYear), 21.0)
        }

    private fun sequenceGuesses(match: SequenceMatch): Double {
        val first = match.token.firstOrNull() ?: '\u0000'
        var guesses =
            when (first) {
                'a', 'A', 'z', 'Z', '0', '1', '9' -> 4.0
                in '0'..'9' -> 10.0
                else -> 26.0
            }
        if (!match.ascending) guesses *= 2
        return guesses * match.token.length
    }

    private fun spatialGuesses(match: SpatialMatch): Double {
        val graph = graphStatistics.getValue(match.graph)
        var guesses = 0.0
        for (length in 2..match.token.length) {
            for (turns in 1..min(match.turns, length - 1)) {
                guesses += binomial(length - 1, turns - 1) * graph.startingPositions * PasswordMath.pow(graph.averageDegree, turns)
            }
        }
        if (match.shiftedCount != 0) {
            val unshifted = match.token.length - match.shiftedCount
            guesses *= if (unshifted == 0) 2.0 else variations(match.shiftedCount, unshifted)
        }
        // JS Math.round resolves half ties toward positive infinity; Kotlin round uses ties to even.
        val roundedDown = floor(guesses)
        return if (guesses - roundedDown < 0.5) roundedDown else roundedDown + 1
    }

    private fun variations(
        firstCount: Int,
        secondCount: Int,
    ): Double {
        var result = 0.0
        for (count in 1..min(firstCount, secondCount)) result += binomial(firstCount + secondCount, count)
        return result
    }

    private fun binomial(
        n: Int,
        k: Int,
    ): Double {
        if (k > n) return 0.0
        var count = n
        var coefficient = 1.0
        for (i in 1..k) {
            coefficient *= count
            coefficient /= i
            count--
        }
        return coefficient
    }

    private fun factorial(n: Int): Double {
        var result = 1.0
        for (i in 2..n) result *= i
        return result
    }

    private fun wordSequenceFactorial(n: Int): Double = if (n <= 1) 1.0 else n * wordSequenceFactorial(n - 1)

    internal fun scoreFromGuesses(guesses: Double): Int =
        when {
            guesses < 1e3 + 5 -> 0
            guesses < 1e6 + 5 -> 1
            guesses < 1e8 + 5 -> 2
            guesses < 1e10 + 5 -> 3
            else -> 4
        }

    private data class GraphStatistics(
        val startingPositions: Int,
        val averageDegree: Double,
    )
}
