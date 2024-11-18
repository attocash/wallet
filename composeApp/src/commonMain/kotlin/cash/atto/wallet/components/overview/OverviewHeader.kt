package cash.atto.wallet.components.overview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoFormatter
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.uistate.overview.OverviewHeaderUiState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun OverviewHeader(
    uiState: OverviewHeaderUiState,
    onSettingsClicked: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(modifier) {
        onSettingsClicked?.let {
            Box(Modifier.align(Alignment.TopEnd)
                .padding(top = 32.dp, end = 16.dp)
                .width(48.dp)
                .height(48.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors
                    .onBackground
                    .copy(alpha = 0.5f)
                )
                .clickable { it.invoke() }
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.Center),
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "settings",
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }

        Column(
            Modifier.align(Alignment.Center)
                .padding(top = 80.dp)
        ) {
            Text(
                text = AttoFormatter.format(uiState.attoCoins),
                style = MaterialTheme.typography.h1
            )
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