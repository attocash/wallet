package cash.atto.wallet.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.atto_welcome_background
import attowallet.composeapp.generated.resources.atto_welcome_cubes_expanded
import attowallet.composeapp.generated.resources.copyright_link
import attowallet.composeapp.generated.resources.copyright_title
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
    val uriHandler = LocalUriHandler.current

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
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineLarge
        )

        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(Res.string.welcome_message),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium
        )

        Box(
            Modifier.padding(top = 18.dp)
                .weight(1f)
        ) {
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

        Text(
            modifier = Modifier.padding(16.dp)
                .padding(bottom = 16.dp),
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    val text = stringResource(Res.string.copyright_title)
                    val link = stringResource(Res.string.copyright_link)
                    append(text)
                    addLink(
                        clickable = LinkAnnotation.Clickable(
                            tag = "URL",
                            linkInteractionListener = {
                                uriHandler.openUri(link)
                            }
                        ),
                        start = 0,
                        end = text.length
                    )
                }
            }
        )
    }
}

@Composable
fun WelcomeScreenExpanded(
    onCreateSecretClicked: () -> Unit,
    onImportSecretClicked: () -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier.fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.atto_welcome_background),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center)
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.welcome_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayLarge
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = stringResource(Res.string.welcome_message),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.height(15.dp))

            AttoButton(
                modifier = Modifier.width(400.dp),
                onClick = onCreateSecretClicked
            ) {
                Text(stringResource(Res.string.welcome_create_wallet))
            }

            AttoOutlinedButton(
                modifier = Modifier.width(400.dp),
                onClick = onImportSecretClicked
            ) {
                Text(stringResource(Res.string.welcome_import_wallet))
            }
        }

        Text(
            modifier = Modifier.align(Alignment.BottomStart)
                .padding(16.dp),
            text = buildAnnotatedString {
                withStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.Underline
                    )) {
                    val text = stringResource(Res.string.copyright_title)
                    val link = stringResource(Res.string.copyright_link)
                    append(text)
                    addLink(
                        clickable = LinkAnnotation.Clickable(
                            tag = "URL",
                            linkInteractionListener = {
                                uriHandler.openUri(link)
                            }
                        ),
                        start = 0,
                        end = text.length
                    )
                }
            }
        )
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