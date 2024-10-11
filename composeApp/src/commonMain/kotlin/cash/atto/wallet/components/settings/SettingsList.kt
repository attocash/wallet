package cash.atto.wallet.components.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.SettingsListUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsList(
    uiState: SettingsListUiState,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier) {
        items(uiState.settings) { setting ->
            SettingItem(setting)
        }
    }
}

@Preview
@Composable
fun SettingsListPreview() {
    AttoWalletTheme {
        SettingsList(SettingsListUiState.PREVIEW)
    }
}