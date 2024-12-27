package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_atto_chevron_left
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppBar(onBackNavigation: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = WindowInsets.systemBars
                    .asPaddingValues()
                    .calculateTopPadding()
            )
            .padding(start = 16.dp, top = 30.dp)
    ) {
        Box(Modifier.size(48.dp, 48.dp)
            .clip(CircleShape)
            .background(
                color = MaterialTheme.colorScheme
                    .onSurface
                    .copy(alpha = 0.05f)
            )
            .clickable { onBackNavigation.invoke() }
        ) {
            Icon(
                modifier = Modifier.align(Alignment.Center),
                imageVector = vectorResource(Res.drawable.ic_atto_chevron_left),
                contentDescription = "backIcon"
            )
        }
    }
}

@Preview
@Composable
fun AppBarPreview() {
    AttoWalletTheme {
        AppBar {}
    }
}