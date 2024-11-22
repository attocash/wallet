package cash.atto.wallet.components

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_nav_overview
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.primaryGradient
import cash.atto.wallet.ui.setting
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (selected)
        Modifier.background(color = MaterialTheme.colors.secondaryVariant)
    else Modifier.background(
        brush = Brush.horizontalGradient(
            MaterialTheme.colors
                .primaryGradient
                .map { it.copy(alpha = 0.4f) }
        )
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .semantics { role = Role.Tab }
            .fillMaxWidth(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colors.primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .then(backgroundModifier)
                .padding(vertical = 24.dp, horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Navigation Icon",
                tint = MaterialTheme.colors.setting
            )

            Text(
                text = label,
                fontSize = 18.sp,
                fontWeight = FontWeight.W300,
                fontFamily = attoFontFamily()
            )
        }
    }
}

@Preview
@Composable
fun NavigationDrawerItemPreview() {
    AttoWalletTheme {
        NavigationDrawerItem(
            label = "Destination",
            icon = vectorResource(Res.drawable.ic_nav_overview),
            selected = true,
            onClick = {}
        )
    }
}