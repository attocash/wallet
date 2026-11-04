package cash.atto.wallet.components.login

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun PlatformTermsAcceptance(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onShowTerms: () -> Unit,
    modifier: Modifier,
) {
    ComposeTermsAcceptance(accepted, onAcceptedChange, onShowTerms, modifier)
}
