package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_chevron_down
import attowallet.composeapp.generated.resources.ic_chevron_up
import attowallet.composeapp.generated.resources.ic_nav_settings
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.ui.setting
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
        .background(brush = Brush.horizontalGradient(
            MaterialTheme.colors
                .primaryGradient
                .map { it.copy(alpha = 0.4f) }
        ))
    ) {
        Row(
            modifier = Modifier.padding(
                start = 32.dp,
                top = 24.dp,
                end = 16.dp,
                bottom = 24.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_nav_settings),
                contentDescription = "Settings Icon",
                tint = MaterialTheme.colors.setting
            )

            Text(
                text = label,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.W300,
                fontFamily = attoFontFamily()
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