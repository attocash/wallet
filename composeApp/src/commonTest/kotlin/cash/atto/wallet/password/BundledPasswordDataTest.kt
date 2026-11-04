package cash.atto.wallet.password

import kotlin.test.Test
import kotlin.test.assertEquals

class BundledPasswordDataTest {
    @Test
    fun `bundled dictionaries preserve every frozen word rank and dictionary metadata`() {
        // Given
        val expected = frozenDictionaries

        // When
        val actual =
            BundledPasswordData.data.dictionaries.map { dictionary ->
                val fingerprint = DataFingerprint()
                dictionary.ranks.forEach { (word, rank) ->
                    fingerprint.addString(word)
                    fingerprint.addInt(rank)
                }
                DictionarySnapshot(
                    dictionary.name,
                    dictionary.ranks.size,
                    dictionary.maximumWordLength,
                    dictionary.isWordSequence,
                    fingerprint.finish(),
                )
            }

        // Then
        assertEquals(expected, actual)
        assertEquals(236_514, actual.sumOf { it.wordCount })
    }

    @Test
    fun `bundled graphs preserve every key and ordered nullable neighbor`() {
        // Given
        val expected = frozenGraphs

        // When
        val actual =
            BundledPasswordData.data.graphs.map { graph ->
                val fingerprint = DataFingerprint()
                graph.adjacency.forEach { (key, neighbors) ->
                    fingerprint.addString(key)
                    fingerprint.addInt(neighbors.size)
                    neighbors.forEach(fingerprint::addString)
                }
                GraphSnapshot(graph.name, graph.adjacency.size, fingerprint.finish())
            }

        // Then
        assertEquals(expected, actual)
    }

    private data class DictionarySnapshot(
        val name: String,
        val wordCount: Int,
        val maximumWordLength: Int,
        val isWordSequence: Boolean,
        val fingerprint: String,
    )

    private data class GraphSnapshot(
        val name: String,
        val keyCount: Int,
        val fingerprint: String,
    )

    // FNV-1a over little-endian Ints: UTF-16 string length followed by its code units.
    // A length of -1 represents null, keeping null distinct from an empty string.
    private class DataFingerprint {
        private var value = 14695981039346656037uL

        fun addInt(number: Int) {
            repeat(4) { byteIndex ->
                val byte = (number ushr (byteIndex * 8)) and 0xff
                value = (value xor byte.toULong()) * 1099511628211uL
            }
        }

        fun addString(text: String?) {
            addInt(text?.length ?: -1)
            text?.forEach { addInt(it.code) }
        }

        fun finish(): String = value.toString(16).padStart(16, '0')
    }

    // Captured independently from the original data.json before its Kotlin migration.
    // SHA-256: 36b6e39d1b9060717e88c67d70bd5e295602a80176f87cfa9afab2eeeecc8727.
    // Dictionary maps retain source insertion order and the last rank for duplicate words.
    private val frozenDictionaries =
        listOf(
            DictionarySnapshot("diceware-common", 7_776, 9, false, "c343cbff01285929"),
            DictionarySnapshot("passwords-common", 49_233, 20, false, "23da1a923d18c605"),
            DictionarySnapshot("commonWords-en", 55_830, 23, false, "81b15b4aca3727ca"),
            DictionarySnapshot("firstnames-en", 4_945, 15, false, "166a469cbcd15ac1"),
            DictionarySnapshot("lastnames-en", 88_799, 13, false, "0791edfb3dd8192e"),
            DictionarySnapshot("wikipedia-en", 29_782, 23, false, "9f08a99c61238c2f"),
            DictionarySnapshot("cardinalNumbers-en", 20, 9, true, "9c2ef0d07b1bfad7"),
            DictionarySnapshot("ordinalNumbers-en", 20, 11, true, "0e5e0057afbdbad2"),
            DictionarySnapshot("daysOfWeek-en", 7, 9, true, "3c050f0a6cf88fc8"),
            DictionarySnapshot("months-en", 12, 9, true, "08ad30498deb188a"),
            DictionarySnapshot("seasons-en", 4, 6, true, "9a1d46c9a499aa44"),
            DictionarySnapshot("timePeriods-en", 6, 9, true, "f07ecb522d2c4fe0"),
            DictionarySnapshot("rainbowColors-en", 7, 6, true, "b8666b3264d99aca"),
            DictionarySnapshot("directions-en", 4, 5, true, "40e5a7d6567f331d"),
            DictionarySnapshot("intermediateDirections-en", 4, 9, true, "b476da587f1296a1"),
            DictionarySnapshot("sizeProgression-en", 6, 6, true, "c1b0a82409ebbf2e"),
            DictionarySnapshot("militaryAlphabet-en", 26, 8, true, "bd2720a6f750c7d5"),
            DictionarySnapshot("planets-en", 9, 7, true, "8bd5107975a500ae"),
            DictionarySnapshot("zodiacSigns-en", 12, 11, true, "2e44336754441220"),
            DictionarySnapshot("chineseZodiac-en", 12, 7, true, "edc55e97c6f7690d"),
        )

    private val frozenGraphs =
        listOf(
            GraphSnapshot("azerty", 95, "1653780bbed93be2"),
            GraphSnapshot("dvorak", 94, "6815764321795812"),
            GraphSnapshot("keypad", 15, "a22e95de428211d5"),
            GraphSnapshot("keypadMac", 16, "cd3b8343bb9b71b2"),
            GraphSnapshot("qwerty", 94, "34a394ff5eec4d6a"),
            GraphSnapshot("qwertz", 96, "39b9b9b933d319e0"),
        )
}
