package cash.atto.wallet

interface AttoDestination {
    val route: String
}

object ConfirmPin : AttoDestination {
    override val route = "confirmPin"
}

object CreatePin : AttoDestination {
    override val route = "createPin"
}

object Overview : AttoDestination {
    override val route = "overview"
}

object SafetyWarning : AttoDestination {
    override val route = "safetyWarning"
}

object SecretBackupConfirmation : AttoDestination {
    override val route = "secretBackupConfirm"
}

object SecretPhrase : AttoDestination {
    override val route = "secretPhrase"
}

object Settings : AttoDestination {
    override val route = "settings"
}

object Welcome : AttoDestination {
    override val route = "welcome"
}