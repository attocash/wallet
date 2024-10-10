package cash.atto.wallet.components.overview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OverviewHeader(
    uiState: OverviewHeaderUiState,
    onSettingsClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onSettingsClicked,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "settings",
            )
        }

        Column(Modifier.align(Alignment.Center)) {
            Text(text = uiState.attoCoins.toString())
        }
    }
}

@Preview
@Composable
fun OverviewHeaderPreview() {
    AttoWalletTheme {
        OverviewHeader(
            uiState = OverviewHeaderUiState.DEFAULT,
            onSettingsClicked = {}
        )
    }
}