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
    object DesktopMain : AttoDestination() {
        override val route = "desktopMain"
    }

    @Serializable
    object ImportSecret : AttoDestination() {
        override val route = "importSecret"
    }

    @Serializable
    object Overview : AttoDestination() {
        override val route = "overview"
    }

    @Serializable
    object Representative : AttoDestination() {
        override val route = "representative"
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
    object SendConfirm : AttoDestination() {
        override val route = "sendConfirm"
    }

    @Serializable
    object SendFrom : AttoDestination() {
        override val route = "sendFrom"
    }

    @Serializable
    object SendResult : AttoDestination() {
        override val route = "sendResult"
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