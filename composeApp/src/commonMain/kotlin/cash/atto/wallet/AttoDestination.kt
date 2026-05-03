package cash.atto.wallet

import kotlinx.serialization.Serializable

@Serializable
sealed class AttoDestination {
    @Serializable
    object CreatePassword : AttoDestination()

    @Serializable
    object DesktopMain : AttoDestination()

    @Serializable
    object ImportPhrase : AttoDestination()

    @Serializable
    object RecoveryPhrase : AttoDestination()

    @Serializable
    object Welcome : AttoDestination()
}
