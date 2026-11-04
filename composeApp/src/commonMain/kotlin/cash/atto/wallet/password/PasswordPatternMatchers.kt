package cash.atto.wallet.password

import kotlin.math.abs

// Ported from @zxcvbn-ts/core 4.2.0 matcher implementations.
internal class PasswordPatternMatchers(
    private val data: PasswordEstimatorData,
    private val referenceYear: Int,
) {
    fun dates(password: String): List<DateMatch> {
        val matches = mutableListOf<DateMatch>()
        for (i in password.indices) {
            for (length in 4..8) {
                if (i + length > password.length) break
                val token = password.substring(i, i + length)
                if (!token.all { it in '0'..'9' }) continue
                val candidates =
                    dateSplits.getValue(length).mapNotNull { (first, second) ->
                        mapDate(
                            listOf(
                                token.substring(0, first).toInt(),
                                token.substring(first, second).toInt(),
                                token.substring(second).toInt(),
                            ),
                        )
                    }
                val best = candidates.minByOrNull { abs(it.year - referenceYear) } ?: continue
                matches += dateMatch(token, i, best, "")
            }
        }
        for (i in password.indices) {
            for (length in 6..10) {
                if (i + length > password.length) break
                val token = password.substring(i, i + length)
                val parts = separatedDate.matchEntire(token) ?: continue
                val date =
                    mapDate(listOf(parts.groupValues[1].toInt(), parts.groupValues[3].toInt(), parts.groupValues[4].toInt()))
                        ?: continue
                matches += dateMatch(token, i, date, parts.groupValues[2])
            }
        }
        return sorted(
            matches.filterIndexed { index, match ->
                matches.withIndex().none { (otherIndex, other) ->
                    index != otherIndex && other.i <= match.i && other.j >= match.j
                }
            },
        )
    }

    fun regex(password: String): List<RegexMatch> =
        recentYear
            .findAll(password)
            .map { match ->
                RegexMatch(match.range.first, match.range.last, match.value, regexName = PasswordRegex.RECENT_YEAR)
            }.toList()

    fun sequences(password: String): List<SequenceMatch> {
        if (password.length < 2) return emptyList()
        val matches = mutableListOf<SequenceMatch>()
        var start = 0
        var lastDelta = password[1].code - password[0].code
        for (index in 2 until password.length) {
            val delta = password[index].code - password[index - 1].code
            if (delta != lastDelta) {
                addSequence(matches, password, start, index - 1, lastDelta)
                start = index - 1
                lastDelta = delta
            }
        }
        addSequence(matches, password, start, password.lastIndex, lastDelta)
        return matches
    }

    fun spatial(password: String): List<SpatialMatch> {
        val matches = mutableListOf<SpatialMatch>()
        for (graph in data.graphs) {
            var start = 0
            while (start < password.lastIndex) {
                var end = start + 1
                var lastDirection = -1
                var turns = 0
                var shiftedCount = if (!graph.name.contains("keypad") && password[start] in shiftedCharacters) 1 else 0
                while (true) {
                    val adjacents = graph.adjacency[password[end - 1].toString()].orEmpty()
                    var found = false
                    if (end < password.length) {
                        for ((direction, adjacent) in adjacents.withIndex()) {
                            val adjacentIndex = adjacent?.indexOf(password[end]) ?: -1
                            if (adjacentIndex != -1) {
                                found = true
                                if (adjacentIndex == 1) shiftedCount++
                                if (lastDirection != direction) {
                                    turns++
                                    lastDirection = direction
                                }
                                break
                            }
                        }
                    }
                    if (found) {
                        end++
                    } else {
                        if (end - start > 2) {
                            matches +=
                                SpatialMatch(
                                    start,
                                    end - 1,
                                    password.substring(start, end),
                                    graph = graph.name,
                                    turns = turns,
                                    shiftedCount = shiftedCount,
                                )
                        }
                        start = end
                        break
                    }
                }
            }
        }
        return sorted(matches)
    }

    fun separators(password: String): List<SeparatorMatch> {
        val counts = mutableMapOf<Char, Int>()
        password.forEach { char ->
            if (char in separatorCharacters) counts[char] = counts.getOrElse(char) { 0 } + 1
        }
        val separator =
            counts.entries
                .maxByOrNull { it.value }
                ?.takeIf { it.value >= 2 }
                ?.key ?: return emptyList()
        val matches = mutableListOf<SeparatorMatch>()
        var index = 0
        // Upstream's global regex consumes the character before the separator as well.
        while (index < password.lastIndex) {
            if (password[index] != separator &&
                password[index] != '\n' &&
                password[index + 1] == separator &&
                (index + 2 == password.length || password[index + 2] != separator)
            ) {
                matches += SeparatorMatch(index + 1, index + 1, separator.toString())
                index += 2
            } else {
                index++
            }
        }
        return matches
    }

    fun repeats(
        password: String,
        analyzeBase: (String) -> Double,
    ): List<RepeatMatch> {
        val matches = mutableListOf<RepeatMatch>()
        var lastIndex = 0
        while (lastIndex < password.length) {
            val greedy = findRepeat(password, lastIndex, greedy = true) ?: break
            val lazy = findRepeat(password, lastIndex, greedy = false) ?: break
            val repeat: RepeatedToken
            val baseToken: String
            if (greedy.token.length > lazy.token.length) {
                repeat = greedy
                baseToken = shortestRepeatedBase(greedy.token)
            } else {
                repeat = lazy
                baseToken = lazy.base
            }
            matches +=
                RepeatMatch(
                    repeat.start,
                    repeat.start + repeat.token.length - 1,
                    repeat.token,
                    baseToken = baseToken,
                    baseGuesses = analyzeBase(baseToken),
                    repeatCount = repeat.token.length / baseToken.length,
                )
            lastIndex = repeat.start + repeat.token.length
        }
        return matches
    }

    fun wordSequences(
        password: String,
        dictionaryMatches: List<DictionaryMatch>,
    ): List<WordSequenceMatch> {
        val sortedMatches =
            dictionaryMatches.sortedWith(
                compareBy<DictionaryMatch> { it.i }
                    .thenBy { it.j }
                    .thenBy { it.reversed }
                    .thenBy { it.l33t }
                    .thenComparator { first, second ->
                        // JS treats the NaN result of rank subtraction as an equal comparison.
                        val difference = first.rank - second.rank
                        when {
                            difference < 0 -> -1
                            difference > 0 -> 1
                            else -> 0
                        }
                    },
            )
        val words = mutableListOf<DictionaryMatch>()
        sortedMatches.forEach { match ->
            if (words.isEmpty() || match.i > words.last().j) words += match
        }
        val matches = mutableListOf<WordSequenceMatch>()
        // Preserve upstream's repeated sequences from every starting word.
        for (start in words.indices) {
            var current = mutableListOf(words[start])
            var currentEnd = words[start].j
            for (next in words.drop(start + 1)) {
                val between = password.substring(current.last().j + 1, next.i)
                val camelCase = between.length == 1 && between == between.uppercase() && between != between.lowercase()
                if (between in wordSeparators || camelCase) {
                    current += next
                    currentEnd = next.j
                } else if (next.i > currentEnd) {
                    if (current.size > 1) matches += wordSequenceMatch(current, password)
                    current = mutableListOf(next)
                    currentEnd = next.j
                }
            }
            if (current.size > 1) matches += wordSequenceMatch(current, password)
        }
        return matches
    }

    private fun addSequence(
        matches: MutableList<SequenceMatch>,
        password: String,
        start: Int,
        end: Int,
        delta: Int,
    ) {
        if ((end - start > 1 || abs(delta) == 1) && abs(delta) in 1..5) {
            matches += SequenceMatch(start, end, password.substring(start, end + 1), ascending = delta > 0)
        }
    }

    private fun findRepeat(
        password: String,
        from: Int,
        greedy: Boolean,
    ): RepeatedToken? {
        for (start in from until password.lastIndex) {
            if (password[start] in lineTerminators) continue
            var runEnd = start
            while (runEnd < password.length && password[runEnd] !in lineTerminators) runEnd++
            val maxLength = (runEnd - start) / 2
            val lengths = if (greedy) maxLength downTo 1 else 1..maxLength
            for (length in lengths) {
                if (!password.regionMatches(start, password, start + length, length)) continue
                var end = start + length * 2
                while (end + length <= runEnd && password.regionMatches(start, password, end, length)) end += length
                return RepeatedToken(start, password.substring(start, end), password.substring(start, start + length))
            }
        }
        return null
    }

    private fun shortestRepeatedBase(token: String): String {
        for (length in 1..token.length / 2) {
            if (token.length % length != 0) continue
            if ((length until token.length step length).all { token.regionMatches(0, token, it, length) }) {
                return token.substring(0, length)
            }
        }
        error("A repeated token must have a repeated base")
    }

    private fun wordSequenceMatch(
        words: List<DictionaryMatch>,
        password: String,
    ): WordSequenceMatch {
        val counts = mutableMapOf<String, Int>()
        var mostCommon = words.first().dictionaryName
        var maxCount = 0
        words.forEach { word ->
            val count = counts.getOrElse(word.dictionaryName) { 0 } + 1
            counts[word.dictionaryName] = count
            if (count > maxCount) {
                maxCount = count
                mostCommon = word.dictionaryName
            }
        }
        return WordSequenceMatch(
            words.first().i,
            words.last().j,
            password.substring(words.first().i, words.last().j + 1),
            dictionaryName = mostCommon,
            ascending = words.zipWithNext().all { (first, second) -> second.rank >= first.rank },
            words = words.map { it.matchedWord },
        )
    }

    private fun dateMatch(
        token: String,
        start: Int,
        date: DateParts,
        separator: String,
    ) = DateMatch(
        start,
        start + token.length - 1,
        token,
        year = date.year,
        month = date.month,
        day = date.day,
        separator = separator,
    )

    private fun mapDate(parts: List<Int>): DateParts? {
        if (parts[1] !in 1..31 ||
            parts.any { it in 100..999 || it > 2050 } ||
            parts.count { it > 31 } >= 2 ||
            parts.all { it > 12 } ||
            parts.count { it <= 0 } >= 2
        ) {
            return null
        }
        val yearSplits = listOf(parts[2] to parts.take(2), parts[0] to parts.drop(1))
        for ((year, rest) in yearSplits) {
            if (year in 1000..2050) {
                val (day, month) = dayMonth(rest) ?: return null
                return DateParts(year, month, day)
            }
        }
        for ((year, rest) in yearSplits) {
            val (day, month) = dayMonth(rest) ?: continue
            val fullYear =
                when {
                    year > 99 -> year
                    year > 50 -> year + 1900
                    else -> year + 2000
                }
            return DateParts(fullYear, month, day)
        }
        return null
    }

    private fun dayMonth(parts: List<Int>): Pair<Int, Int>? {
        if (parts[0] in 1..31 && parts[1] in 1..12) return parts[0] to parts[1]
        if (parts[1] in 1..31 && parts[0] in 1..12) return parts[1] to parts[0]
        return null
    }

    private fun <T : PasswordMatch> sorted(matches: List<T>): List<T> = matches.sortedWith(compareBy({ it.i }, { it.j }))

    private data class DateParts(
        val year: Int,
        val month: Int,
        val day: Int,
    )

    private data class RepeatedToken(
        val start: Int,
        val token: String,
        val base: String,
    )

    private companion object {
        val recentYear = Regex("19[0-9][0-9]|200[0-9]|201[0-9]|202[0-9]")
        val separatedDate =
            Regex(
                "([0-9]{1,4})([\\t\\n\\u000b\\u000c\\r \\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff/\\\\_.-])" +
                    "([0-9]{1,2})\\2([0-9]{1,4})",
            )

        // Upstream's joined character class escapes '_' with its backslash entry.
        val separatorCharacters = " ,;:|/_.-"
        val shiftedCharacters = "~!@#$%^&*()_+QWERTYUIOP{}|ASDFGHJKL:\"ZXCVBNM<>?"
        val lineTerminators = "\n\r\u2028\u2029"
        val wordSeparators = setOf("", " ", "-", "_", ".")
        val dateSplits =
            mapOf(
                4 to listOf(1 to 2, 2 to 3),
                5 to listOf(1 to 3, 2 to 3, 2 to 4),
                6 to listOf(1 to 2, 2 to 4, 4 to 5),
                7 to listOf(1 to 3, 2 to 3, 4 to 5, 4 to 6),
                8 to listOf(2 to 4, 4 to 6),
            )
    }
}
