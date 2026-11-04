package cash.atto.wallet.password

// Port of zxcvbn-ts 4.2.0. Attribution: commonMain/composeResources/files/password-strength-estimator-notices.txt.
internal class PasswordDictionaryMatcher(
    private val data: PasswordEstimatorData,
) {
    private val substitutionTrie = buildSubstitutionTrie()

    fun dictionary(
        password: String,
        userInputs: List<String>? = null,
        wordSequencesOnly: Boolean = false,
    ): List<DictionaryMatch> {
        val dictionaries =
            if (wordSequencesOnly) {
                data.dictionaries.filter { it.isWordSequence }
            } else if (userInputs == null) {
                data.dictionaries
            } else {
                data.dictionaries +
                    PasswordDictionary(
                        name = "userInputs",
                        ranks = userInputs.mapIndexed { index, word -> word.lowercase() to index + 1 }.toMap(),
                        maximumWordLength = userInputs.maxOfOrNull { it.length } ?: 0,
                    )
            }
        val maximumLength = dictionaries.maxOfOrNull { it.maximumWordLength } ?: 0
        val lower = password.lowercase()
        val matches = mutableListOf<DictionaryMatch>()
        for (i in password.indices) {
            for (j in i until password.length) {
                val isFullPassword = i == 0 && j == password.lastIndex
                if (j - i + 1 > maximumLength && !isFullPassword) break
                val word = lower.substring(i, j + 1)
                for (dictionary in dictionaries) {
                    if (j - i + 1 > dictionary.maximumWordLength && !isFullPassword) continue
                    // Reference dictionaries are JS plain objects: these inherited keys count as non-numeric matches.
                    val rank =
                        if (word == "__proto__") {
                            Double.NaN
                        } else {
                            dictionary.ranks[word]?.toDouble()
                                ?: if (word == "constructor") Double.NaN else continue
                        }
                    matches.add(
                        DictionaryMatch(
                            i = i,
                            j = j,
                            token = password.substring(i, j + 1),
                            dictionaryName = dictionary.name,
                            matchedWord = word,
                            rank = rank,
                        ),
                    )
                }
            }
        }
        return matches
    }

    fun reversed(
        password: String,
        userInputs: List<String>? = null,
        wordSequencesOnly: Boolean = false,
    ): List<DictionaryMatch> =
        dictionary(reverseCodeUnits(password), userInputs, wordSequencesOnly).map { match ->
            match.copy(
                token = reverseCodeUnits(match.token),
                reversed = true,
                i = password.length - 1 - match.j,
                j = password.length - 1 - match.i,
            )
        }

    fun l33t(
        password: String,
        userInputs: List<String>? = null,
        wordSequencesOnly: Boolean = false,
    ): List<DictionaryMatch> {
        val matches = mutableListOf<DictionaryMatch>()
        val seen = mutableSetOf<DictionaryMatchKey>()
        var hasFullMatch = false
        for (variant in CleanPasswords(password, substitutionTrie).getAll()) {
            if (hasFullMatch) break
            for (match in dictionary(variant.password, userInputs, wordSequencesOnly)) {
                var originalStart = match.i
                var originalEnd = match.j
                val substitutions = mutableListOf<PasswordSubstitution>()
                val seenSubstitutions = mutableSetOf<Pair<String, String>>()
                for (change in variant.changes) {
                    val difference = change.substitution.length - change.letter.length
                    if (change.i < match.i) {
                        originalStart += difference
                        originalEnd += difference
                    } else if (change.i <= match.j) {
                        originalEnd += difference
                        if (seenSubstitutions.add(change.letter to change.substitution)) {
                            substitutions.add(change)
                        }
                    }
                }
                if (!hasFullMatch) hasFullMatch = originalStart == 0 && originalEnd == password.lastIndex
                val token = password.substring(originalStart, minOf(originalEnd + 1, password.length))
                val key = DictionaryMatchKey(originalStart, originalEnd, match.dictionaryName, match.matchedWord)
                if (token.lowercase() != match.matchedWord && seen.add(key)) {
                    matches.add(
                        match.copy(
                            i = originalStart,
                            j = originalEnd,
                            token = token,
                            l33t = true,
                            substitutions = substitutions,
                        ),
                    )
                }
            }
        }
        return matches.filter { it.token.length > 1 }
    }

    private fun reverseCodeUnits(value: String): String =
        buildString(value.length) {
            // JS split('').reverse() reverses UTF-16 units, including surrogate halves.
            for (index in value.lastIndex downTo 0) append(value[index])
        }

    private fun buildSubstitutionTrie(): SubstitutionNode {
        val root = SubstitutionNode("")
        for ((letter, substitutions) in PasswordLeetSubstitutions.table) {
            for (substitution in substitutions) {
                var current = root
                for (character in substitution) {
                    current = current.children.getOrPut(character) { SubstitutionNode(current.path + character) }
                }
                current.letters.add(letter)
            }
        }
        return root
    }

    private data class DictionaryMatchKey(
        val i: Int,
        val j: Int,
        val dictionaryName: String,
        val word: String,
    )

    private class SubstitutionNode(
        val path: String,
    ) {
        val children = mutableMapOf<Char, SubstitutionNode>()
        val letters = mutableListOf<String>()
    }

    private data class CleanPassword(
        val password: String,
        val changes: List<PasswordSubstitution>,
    )

    private class CleanPasswords(
        private val password: String,
        private val root: SubstitutionNode,
    ) {
        private val buffer = mutableListOf<String>()
        private val results = mutableListOf<CleanPassword>()

        fun getAll(): List<CleanPassword> {
            collect(true, true, 0, 0, emptyList(), null, 0)
            collect(false, true, 0, 0, emptyList(), null, 0)
            return results
        }

        private fun nodesAt(index: Int): List<SubstitutionNode> {
            val nodes = mutableListOf<SubstitutionNode>()
            var current = root
            for (position in index until password.length) {
                current = current.children[password[position]] ?: break
                nodes.add(current)
            }
            return nodes
        }

        private fun collect(
            onlyFullSubstitution: Boolean,
            isFullSubstitution: Boolean,
            index: Int,
            substitutedIndex: Int,
            changes: List<PasswordSubstitution>,
            lastSubstitution: String?,
            consecutiveCount: Int,
        ) {
            if (results.size >= 100) return
            if (index == password.length) {
                if (onlyFullSubstitution == isFullSubstitution) {
                    results.add(CleanPassword(buffer.joinToString(""), changes))
                }
                return
            }
            val nodes = nodesAt(index)
            var hasSubstitutions = false
            for (node in nodes.asReversed()) {
                if (node.letters.isEmpty()) continue
                val substitution = node.path
                if (lastSubstitution == substitution && consecutiveCount >= 3) continue
                hasSubstitutions = true
                for (letter in node.letters) {
                    buffer.add(letter)
                    collect(
                        onlyFullSubstitution,
                        isFullSubstitution,
                        index + substitution.length,
                        substitutedIndex + letter.length,
                        changes + PasswordSubstitution(substitutedIndex, letter, substitution),
                        substitution,
                        if (lastSubstitution == substitution) consecutiveCount + 1 else 1,
                    )
                    buffer.removeAt(buffer.lastIndex)
                    if (results.size >= 100) return
                }
            }
            if (!onlyFullSubstitution || !hasSubstitutions) {
                buffer.add(password[index].toString())
                collect(
                    onlyFullSubstitution,
                    isFullSubstitution && !hasSubstitutions,
                    index + 1,
                    substitutedIndex + 1,
                    changes,
                    lastSubstitution,
                    consecutiveCount,
                )
                buffer.removeAt(buffer.lastIndex)
            }
        }
    }
}
