package cash.atto.wallet.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.settings.SettingsListUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingsList(
    uiState: SettingsListUiState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding
    ) {
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