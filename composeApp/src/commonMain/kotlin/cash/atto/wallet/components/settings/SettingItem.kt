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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider
import cash.atto.wallet.ui.setting
import cash.atto.wallet.uistate.settings.SettingItemUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SettingItem(uiState: SettingItemUiState) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clickable { uiState.onClick.invoke() }
            .padding(16.dp)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = uiState.icon,
                contentDescription = uiState.title,
                tint = MaterialTheme.colors.setting
            )

            Column {
                Text(
                    text = uiState.title,
                    style = MaterialTheme.typography.h6
                )
            }
        }
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