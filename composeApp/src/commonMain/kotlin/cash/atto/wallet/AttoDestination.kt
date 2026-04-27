package cash.atto.wallet

import kotlinx.serialization.Serializable

@Serializable
sealed class AttoDestination {
    abstract val route: String

    @Serializable
    object BackupSecret : AttoDestination() {
        override val route = "backupSecret"
    }

    @Serializable
    object CreatePassword : AttoDestination() {
        override val route = "createPassword"
    }

    @Serializable
    object DesktopMain : AttoDestination() {
        override val route = "desktopMain"
    }

    @Serializable
    object ImportPhrase : AttoDestination() {
        override val route = "import-phrase"
    }

    @Serializable
    object Overview : AttoDestination() {
        override val route = "overview"
    }

    @Serializable
    data class VoterDetail(
        val voterAddress: String,
    ) : AttoDestination() {
        override val route = "voterDetail"
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
    object RecoveryPhrase : AttoDestination() {
        override val route = "recovery-phrase"
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
