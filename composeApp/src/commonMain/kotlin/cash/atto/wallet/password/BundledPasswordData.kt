package cash.atto.wallet.password

import cash.atto.wallet.password.data.commonEnglishWords
import cash.atto.wallet.password.data.commonPasswordWords
import cash.atto.wallet.password.data.dicewareWords
import cash.atto.wallet.password.data.firstNameWords
import cash.atto.wallet.password.data.lastNameWords
import cash.atto.wallet.password.data.passwordKeyboardGraphs
import cash.atto.wallet.password.data.wikipediaWords
import cash.atto.wallet.password.data.wordSequenceWords

internal object BundledPasswordData {
    val data: PasswordEstimatorData by lazy {
        val dictionaries =
            listOf(
                "diceware-common" to dicewareWords(),
                "passwords-common" to commonPasswordWords(),
                "commonWords-en" to commonEnglishWords(),
                "firstnames-en" to firstNameWords(),
                "lastnames-en" to lastNameWords(),
                "wikipedia-en" to wikipediaWords(),
            ) + wordSequenceWords()
        PasswordEstimatorData(
            dictionaries = dictionaries.map { (name, words) -> dictionary(name, words) },
            graphs = passwordKeyboardGraphs(),
        )
    }

    private fun dictionary(
        name: String,
        words: Sequence<String>,
    ): PasswordDictionary {
        var maximumWordLength = 0
        val ranks =
            buildMap {
                words.forEachIndexed { index, word ->
                    // As in Options.buildRankedDictionary, the last occurrence owns the rank.
                    put(word, index + 1)
                    maximumWordLength = maxOf(maximumWordLength, word.length)
                }
            }
        return PasswordDictionary(
            name = name,
            ranks = ranks,
            maximumWordLength = maximumWordLength,
            isWordSequence = wordSequenceNames.any { name == it || name.startsWith("$it-") },
        )
    }

    // Options.wordSequenceNames from @zxcvbn-ts/core 4.2.0.
    private val wordSequenceNames =
        setOf(
            "cardinalNumbers",
            "ordinalNumbers",
            "daysOfWeek",
            "months",
            "seasons",
            "timePeriods",
            "rainbowColors",
            "directions",
            "intermediateDirections",
            "sizeProgression",
            "militaryAlphabet",
            "planets",
            "zodiacSigns",
            "chineseZodiac",
        )
}
