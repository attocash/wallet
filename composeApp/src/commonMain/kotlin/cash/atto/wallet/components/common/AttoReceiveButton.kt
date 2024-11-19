package cash.atto.wallet.components.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_arrow_down
import attowallet.composeapp.generated.resources.overview_receive
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.secondaryGradient
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun AttoReceiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                onClick = onClick
            ),
        shape = RoundedCornerShape(62.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = MaterialTheme.colors.secondaryGradient
                    )
                )
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.overview_receive),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colors.onSecondary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.button
            )

            Box(Modifier.size(52.dp, 52.dp)
                .clip(CircleShape)
                .background(color = MaterialTheme.colors.secondaryVariant)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_arrow_down),
                    contentDescription = "send icon",
                    modifier = Modifier.align(Alignment.Center),
                    tint = MaterialTheme.colors.secondary
                )
            }
        }
    }
}

@Preview
@Composable
fun AttoReceiveButtonPreview() {
    AttoWalletTheme {
        AttoReceiveButton(onClick = {})
    }
}