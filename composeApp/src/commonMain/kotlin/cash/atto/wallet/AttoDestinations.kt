package cash.atto.wallet

import kotlinx.serialization.Serializable

@Serializable
sealed class AttoDestination {
    abstract val route: String

    @Serializable
    object ConfirmPin : AttoDestination() {
        override val route = "confirmPin"
    }

    @Serializable
    object CreatePin : AttoDestination() {
        override val route = "createPin"
    }

    @Serializable
    object Overview : AttoDestination() {
        override val route = "overview"
    }

    @Serializable
    object SafetyWarning : AttoDestination() {
        override val route = "safetyWarning"
    }

    @Serializable
    object SecretBackupConfirmation : AttoDestination() {
        override val route = "secretBackupConfirm"
    }

    @Serializable
    object SecretPhrase : AttoDestination() {
        override val route = "secretPhrase"
    }

    @Serializable
    object Settings : AttoDestination() {
        override val route = "settings"
    }

    @Serializable
    object Welcome : AttoDestination() {
        override val route = "welcome"
    }
}