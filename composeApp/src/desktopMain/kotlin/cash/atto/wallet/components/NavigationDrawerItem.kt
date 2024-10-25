package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .semantics { role = Role.Tab }
            .height(48.dp)
            .fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(end = 24.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.fillMaxHeight()
                        .width(4.dp)
                        .background(
                            color = if (selected)
                                MaterialTheme.colors.primary
                            else MaterialTheme.colors.surface
                        )
                )

                Surface(
                    modifier = Modifier.weight(1f)
                        .padding(start = 16.dp),
                    contentColor = if (selected)
                        MaterialTheme.colors.primary
                    else MaterialTheme.colors.onSurface
                ) {
                    label()
                }
            }

            Divider(
                modifier = Modifier.padding(start = 4.dp),
                color = MaterialTheme.colors.divider
            )
        }
    }
}

@Preview
@Composable
fun NavigationDrawerItemPreview() {
    AttoWalletTheme {
        NavigationDrawerItem(
            label = { Text("Destination") },
            selected = true,
            onClick = {}
        )
    }
}