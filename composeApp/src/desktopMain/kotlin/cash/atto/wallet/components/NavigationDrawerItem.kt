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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.primaryGradient

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun NavigationDrawerItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (selected)
        Modifier.background(color = MaterialTheme.colors.secondaryVariant)
    else Modifier.background(
        brush = Brush.horizontalGradient(MaterialTheme.colors.primaryGradient)
    )

    Surface(
        selected = selected,
        onClick = onClick,
        modifier = modifier
            .semantics { role = Role.Tab }
            .height(48.dp)
            .fillMaxWidth(),
        contentColor = MaterialTheme.colors.primary
    ) {
        Box(Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .then(backgroundModifier)
            .padding(start = 16.dp, end = 24.dp)
        ) {
            Text(
                text = label,
                modifier = Modifier.align(Alignment.Center)
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
            selected = true,
            onClick = {}
        )
    }
}