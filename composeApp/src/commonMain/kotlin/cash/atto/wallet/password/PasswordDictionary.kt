package cash.atto.wallet.password

internal data class PasswordDictionary(
    val name: String,
    val ranks: Map<String, Int>,
    val maximumWordLength: Int,
    val isWordSequence: Boolean = false,
)

internal data class PasswordKeyboardGraph(
    val name: String,
    val adjacency: Map<String, List<String?>>,
)

internal data class PasswordEstimatorData(
    val dictionaries: List<PasswordDictionary>,
    val graphs: List<PasswordKeyboardGraph>,
)
