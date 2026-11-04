package cash.atto.wallet.password

internal sealed interface PasswordMatch {
    val i: Int
    val j: Int
    val token: String
    val pattern: String
}

internal data class DictionaryMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    // JS inherited dictionary properties have a non-numeric rank; NaN preserves their reference scoring behavior.
    val rank: Double,
    val dictionaryName: String = "",
    val matchedWord: String = "",
    val reversed: Boolean = false,
    val l33t: Boolean = false,
    val substitutions: List<PasswordSubstitution> = emptyList(),
) : PasswordMatch {
    override val pattern: String get() = "dictionary"
}

internal data class SpatialMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    val graph: String,
    val turns: Int,
    val shiftedCount: Int,
) : PasswordMatch {
    override val pattern: String get() = "spatial"
}

internal data class RepeatMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    val baseToken: String,
    val baseGuesses: Double,
    val repeatCount: Int,
) : PasswordMatch {
    override val pattern: String get() = "repeat"
}

internal data class SequenceMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    val ascending: Boolean = true,
) : PasswordMatch {
    override val pattern: String get() = "sequence"
}

internal enum class PasswordRegex {
    RECENT_YEAR,
    ALPHA_LOWER,
    ALPHA_UPPER,
    ALPHA,
    ALPHANUMERIC,
    DIGITS,
    SYMBOLS,
}

internal data class RegexMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    val regexName: PasswordRegex = PasswordRegex.RECENT_YEAR,
) : PasswordMatch {
    override val pattern: String get() = "regex"
}

internal data class DateMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    val year: Int,
    val month: Int,
    val day: Int,
    val separator: String,
) : PasswordMatch {
    override val pattern: String get() = "date"
}

internal data class BruteForceMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
) : PasswordMatch {
    override val pattern: String get() = "bruteforce"
}

internal data class SeparatorMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
) : PasswordMatch {
    override val pattern: String get() = "separator"
}

internal data class WordSequenceMatch(
    override val i: Int,
    override val j: Int,
    override val token: String,
    val words: List<String>,
    val dictionaryName: String,
    val ascending: Boolean,
) : PasswordMatch {
    override val pattern: String get() = "wordSequence"
}

internal data class PasswordSubstitution(
    val i: Int,
    val letter: String,
    val substitution: String,
)
