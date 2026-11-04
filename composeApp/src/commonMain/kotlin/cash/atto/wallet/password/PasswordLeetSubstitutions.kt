package cash.atto.wallet.password

// Fixed zxcvbn-ts 4.2.0 substitution data.
internal object PasswordLeetSubstitutions {
    val table: Map<String, List<String>> =
        mapOf(
            "a" to listOf("4", "@"),
            "b" to listOf("8"),
            "c" to listOf("(", "{", "[", "<"),
            "d" to listOf("6", "|)"),
            "e" to listOf("3"),
            "f" to listOf("#"),
            "g" to listOf("6", "9", "&"),
            "h" to listOf("#", "|-|"),
            "i" to listOf("1", "!", "|"),
            "k" to listOf("<", "|<"),
            "l" to listOf("!", "1", "|", "7"),
            "m" to listOf("^^", "nn", "2n", "/\\\\/\\\\"),
            "n" to listOf("//"),
            "o" to listOf("0", "()"),
            "q" to listOf("9"),
            "u" to listOf("|_|"),
            "s" to listOf("\$", "5"),
            "t" to listOf("+", "7"),
            "v" to listOf("<", ">", "/"),
            "w" to listOf("^/", "uu", "vv", "2u", "2v", "\\\\/\\\\/"),
            "x" to listOf("%", "><"),
            "z" to listOf("2"),
        )
}
