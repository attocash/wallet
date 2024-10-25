package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider

@Composable
fun ExpandableDrawerItem(
    label: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val drawerOpened = remember {
        mutableStateOf(false)
    }

    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(48.dp)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f)
                    .padding(start = 20.dp)
            ) { label() }

            Icon(
                imageVector = if (drawerOpened.value)
                    Icons.Outlined.Close
                else Icons.Outlined.Check,
                contentDescription = "Drawer toggle",
                modifier = Modifier
                    .clickable { drawerOpened.value = !drawerOpened.value }
                    .padding(8.dp)
            )
        }

        Divider(
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colors.divider
        )

        if (drawerOpened.value)
            content()
    }
}

@Preview
@Composable
fun ExpandableDrawerItemPreview() {
    AttoWalletTheme {
        ExpandableDrawerItem(
            label = { Text("Label") }
        ) {
            Text("Content")
        }
    }
}