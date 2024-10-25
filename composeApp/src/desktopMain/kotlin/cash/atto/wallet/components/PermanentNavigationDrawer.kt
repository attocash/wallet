package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme

@Composable
fun PermanentNavigationDrawer(
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxSize()
            .background(color = MaterialTheme.colors.surface)
    ) {
        Surface(Modifier.width(280.dp)) {
            drawerContent()
        }

        Box(
            modifier = Modifier.fillMaxHeight()
                .width(4.dp)
                .background(color = MaterialTheme.colors.background)
        )

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