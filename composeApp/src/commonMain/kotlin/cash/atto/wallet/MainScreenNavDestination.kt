package cash.atto.wallet

import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.main_nav_overview
import attowallet.composeapp.generated.resources.main_nav_receive
import attowallet.composeapp.generated.resources.main_nav_send
import attowallet.composeapp.generated.resources.main_nav_settings
import attowallet.composeapp.generated.resources.main_nav_staking
import attowallet.composeapp.generated.resources.main_nav_transactions
import org.jetbrains.compose.resources.StringResource

enum class MainScreenNavDestination(
    val destinationName: StringResource,
) {
    OVERVIEW(Res.string.main_nav_overview),
    SEND(Res.string.main_nav_send),
    RECEIVE(Res.string.main_nav_receive),
    TRANSACTIONS(Res.string.main_nav_transactions),
    SETTINGS(Res.string.main_nav_settings),
    STAKING(Res.string.main_nav_staking),
}
