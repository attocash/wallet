package cash.atto.wallet.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_welcome_background
import attowallet.composeapp.generated.resources.atto_welcome_cubes
import attowallet.composeapp.generated.resources.atto_welcome_cubes_expanded
import attowallet.composeapp.generated.resources.ic_atto
import attowallet.composeapp.generated.resources.welcome_create_wallet
import attowallet.composeapp.generated.resources.welcome_import_wallet
import attowallet.composeapp.generated.resources.welcome_message
import attowallet.composeapp.generated.resources.welcome_title
import cash.atto.wallet.components.common.AttoButton
import cash.atto.wallet.components.common.AttoOutlinedButton
import cash.atto.wallet.ui.AttoWalletTheme
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WelcomeScreen(
    onCreateSecretClicked: () -> Unit,
    onImportSecretClicked: () -> Unit
) {
    val windowSizeClass = calculateWindowSizeClass()

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
        WelcomeScreenCompact(
            onCreateSecretClicked = onCreateSecretClicked,
            onImportSecretClicked = onImportSecretClicked
        )
    } else {
        WelcomeScreenExpanded(
            onCreateSecretClicked = onCreateSecretClicked,
            onImportSecretClicked = onImportSecretClicked
        )
    }
}

@Composable
fun WelcomeScreenCompact(
    onCreateSecretClicked: () -> Unit,
    onImportSecretClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.atto_welcome_background),
                contentScale = ContentScale.FillBounds
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(160.dp))

        Image(
            imageVector = vectorResource(Res.drawable.ic_atto),
            contentDescription = "Atto Wallet main icon"
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.welcome_title),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.h4
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.welcome_message),
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption
        )

        Box(Modifier.padding(top = 18.dp)
            .weight(1f)
        ) {
            Image(
                bitmap = imageResource(Res.drawable.atto_welcome_cubes),
                contentDescription = "Atto Wallet",
                modifier = Modifier.align(Alignment.TopCenter),
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AttoButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onCreateSecretClicked
                ) {
                    Text(stringResource(Res.string.welcome_create_wallet))
                }

                AttoOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onImportSecretClicked
                ) {
                    Text(stringResource(Res.string.welcome_import_wallet))
                }
            }
        }
    }
}

@Composable
fun WelcomeScreenExpanded(
    onCreateSecretClicked: () -> Unit,
    onImportSecretClicked: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.atto_welcome_background),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Image(
            bitmap = imageResource(Res.drawable.atto_welcome_cubes_expanded),
            contentDescription = "Atto Wallet",
            modifier = Modifier.align(Alignment.BottomEnd)
                .padding(start = 450.dp)
        )

        Column(
            modifier = Modifier.width(700.dp)
                .align(Alignment.CenterStart)
                .padding(
                    start = 160.dp,
                    top = 40.dp
                ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = stringResource(Res.string.welcome_title),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.h1
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.welcome_message),
                color = MaterialTheme.colors.onSurface,
                style = MaterialTheme.typography.h6
            )

            Spacer(Modifier.height(15.dp))

            AttoButton(
                modifier = Modifier.width(260.dp),
                onClick = onCreateSecretClicked
            ) {
                Text(stringResource(Res.string.welcome_create_wallet))
            }

            AttoOutlinedButton(
                modifier = Modifier.width(260.dp),
                onClick = onImportSecretClicked
            ) {
                Text(stringResource(Res.string.welcome_import_wallet))
            }
        }
    }
}

@Composable
@Preview
fun WelcomeScreenCompactPreview() {
    AttoWalletTheme {
        WelcomeScreenCompact(
            onCreateSecretClicked = {},
            onImportSecretClicked = {}
        )
    }
}

@Composable
@Preview
fun WelcomeScreenExpandedPreview() {
    AttoWalletTheme {
        WelcomeScreenExpanded(
            onCreateSecretClicked = {},
            onImportSecretClicked = {}
        )
    }
}