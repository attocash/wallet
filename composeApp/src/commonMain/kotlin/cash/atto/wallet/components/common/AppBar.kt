package cash.atto.wallet.components.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AppBar(onBackNavigation: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        Icon(
            modifier = Modifier
                .clickable { onBackNavigation.invoke() }
                .padding(16.dp),
            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "backIcon"
        )
    }
}

@Preview
@Composable
fun AppBarPreview() {
    AttoWalletTheme {
        AppBar {}
    }
}