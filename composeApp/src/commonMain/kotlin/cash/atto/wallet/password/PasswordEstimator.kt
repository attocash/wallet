package cash.atto.wallet.password

// Fixed zxcvbn-ts 4.2.0 configuration: common/English data, 256 UTF-16 units, and wallet context.
internal class PasswordEstimator(
    data: PasswordEstimatorData = BundledPasswordData.data,
    referenceYear: Int = currentPasswordReferenceYear(),
) {
    private val dictionary = PasswordDictionaryMatcher(data)
    private val patterns = PasswordPatternMatchers(data, referenceYear)
    private val scoring = PasswordScoring(data, referenceYear)
    private val context = listOf("atto", "wallet", "attocash", "attowallet", "atto wallet")

    fun analyze(password: String): PasswordAnalysis = analyze(password.take(256), context)

    private fun analyze(
        password: String,
        userInputs: List<String>?,
    ): PasswordAnalysis {
        val matches = mutableListOf<PasswordMatch>()
        matches.addAll(patterns.dates(password))
        matches.addAll(dictionary.dictionary(password, userInputs))
        matches.addAll(dictionary.l33t(password, userInputs))
        matches.addAll(dictionary.reversed(password, userInputs))
        matches.addAll(patterns.regex(password))
        matches.addAll(patterns.repeats(password) { base -> analyze(base, null).guesses })
        matches.addAll(patterns.sequences(password))
        matches.addAll(patterns.spatial(password))
        matches.addAll(patterns.separators(password))
        val sequenceWords =
            dictionary.dictionary(password, wordSequencesOnly = true) +
                dictionary.l33t(password, wordSequencesOnly = true) +
                dictionary.reversed(password, wordSequencesOnly = true)
        matches.addAll(patterns.wordSequences(password, sequenceWords))
        return scoring.analyze(password, matches.sortedWith(compareBy({ it.i }, { it.j })))
    }
}
