package cash.atto.wallet

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class AttoDestination {
    @Serializable
    @SerialName("loading")
    object Loading : AttoDestination()

    @Serializable
    @SerialName("login")
    object Login : AttoDestination()

    @Serializable
    @SerialName("create-password")
    object CreatePassword : AttoDestination()

    @Serializable
    @SerialName("wallet")
    object DesktopMain : AttoDestination()

    @Serializable
    @SerialName("import-phrase")
    object ImportPhrase : AttoDestination()

    @Serializable
    @SerialName("recovery-phrase")
    object RecoveryPhrase : AttoDestination()

    @Serializable
    @SerialName("welcome")
    object Welcome : AttoDestination()
}
