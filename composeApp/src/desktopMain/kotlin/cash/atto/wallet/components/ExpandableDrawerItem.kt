package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_chevron_down
import attowallet.composeapp.generated.resources.ic_chevron_up
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.divider
import cash.atto.wallet.ui.primaryGradient
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ExpandableDrawerItem(
    label: String,
    content: @Composable () -> Unit
) {
    val drawerOpened = remember {
        mutableStateOf(false)
    }

    Column(Modifier.fillMaxWidth()
        .clip(MaterialTheme.shapes.medium)
        .background(
            brush = Brush.horizontalGradient(MaterialTheme.colors.primaryGradient)
        )
    ) {
        Row(
            modifier = Modifier.height(48.dp)
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f)
                    .padding(start = 20.dp),
                color = MaterialTheme.colors.primary
            )

            Icon(
                imageVector = if (drawerOpened.value)
                    vectorResource(Res.drawable.ic_chevron_up)
                else vectorResource(Res.drawable.ic_chevron_down),
                contentDescription = "Drawer toggle",
                modifier = Modifier
                    .clickable { drawerOpened.value = !drawerOpened.value }
                    .padding(8.dp)
            )
        }

        if (drawerOpened.value)
            content()
    }
}

@Preview
@Composable
fun ExpandableDrawerItemPreview() {
    AttoWalletTheme {
        ExpandableDrawerItem(
            label = "Label"
        ) {
            Text("Content")
        }
    }
}