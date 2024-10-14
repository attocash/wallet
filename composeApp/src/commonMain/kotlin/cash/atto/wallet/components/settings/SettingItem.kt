package cash.atto.wallet.components.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider
import cash.atto.wallet.uistate.settings.SettingItemUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingItem(uiState: SettingItemUiState) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clickable { uiState.onClick.invoke() }
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = uiState.icon,
                contentDescription = uiState.title
            )

            Column {
                Text(uiState.title)
            }
        }

        Divider(color = MaterialTheme.colors.divider)
    }
}

@Preview
@Composable
fun SettingItemPreview() {
    AttoWalletTheme {
        SettingItem(
            SettingItemUiState(
                icon = Icons.Filled.Person,
                title = "Contacts",
                onClick = {}
            )
        )
    }
}