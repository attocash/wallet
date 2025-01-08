package cash.atto.wallet.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.ic_lock
import attowallet.composeapp.generated.resources.safety_warning_confirm
import attowallet.composeapp.generated.resources.safety_warning_description
import attowallet.composeapp.generated.resources.safety_warning_hint
import attowallet.composeapp.generated.resources.safety_warning_title
import cash.atto.wallet.components.common.AppBar
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.ui.AttoWalletTheme
import cash.atto.wallet.ui.attoFontFamily
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun SafetyWarningScreen(
    onBackNavigation: () -> Unit,
    onConfirmClicked: () -> Unit,
) {
    Scaffold(
        topBar = { AppBar(onBackNavigation) },
        containerColor = MaterialTheme.colorScheme.surface,
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(
                        start = 16.dp,
                        top = 20.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.safety_warning_title),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.W300,
                        fontFamily = attoFontFamily()
                    )

                    Text(
                        text = stringResource(Res.string.safety_warning_description),
                        lineHeight = 22.sp,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Row(
                        modifier = Modifier.padding(top = 8.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(color = MaterialTheme.colorScheme.secondary)
                            .padding(
                                start = 20.dp,
                                top = 20.dp,
                                end = 40.dp,
                                bottom = 20.dp
                            ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_lock),
                            contentDescription = "Lock icon",
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = stringResource(Res.string.safety_warning_hint),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            fontFamily = attoFontFamily(),
                            lineHeight = 22.sp
                        )
                    }
                }

                AttoButton(
                    onClick = onConfirmClicked,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(Res.string.safety_warning_confirm))
                }
            }
        }
    )
}

@Preview
@Composable
fun SafetyWarningScreenPreview() {
    AttoWalletTheme {
        SafetyWarningScreen({}, {})
    }
}