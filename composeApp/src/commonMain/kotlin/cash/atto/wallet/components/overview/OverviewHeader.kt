package cash.atto.wallet.components.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
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
    onSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
//        border = BorderStroke(1.dp, attoColors.primary),
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary
    ) {
        Box {
            Icon(
                modifier = Modifier.align(Alignment.TopEnd)
                    .clickable { onSettingsClicked.invoke() }
                    .padding(16.dp),
                imageVector = Icons.Outlined.Settings,
                contentDescription = "settings"
            )

            Column(
                Modifier.align(Alignment.Center)
                    .padding(vertical = 8.dp)
            ) {
                Text(text = uiState.attoCoins.toString())
            }
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