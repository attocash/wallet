package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme

@Composable
fun PermanentNavigationDrawer(
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize()
            .then(modifier),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Surface(
            modifier = Modifier.width(450.dp),
            color = Color.Transparent
        ) {
            drawerContent()
        }

        Box { content() }
    }
}

@Preview
@Composable
fun PermanentNavigationDrawerPreview() {
    AttoWalletTheme {
        PermanentNavigationDrawer(
            drawerContent = {
                NavigationDrawerItemPreview()
            }
        ) {
            Text("Hello")
        }
    }
}