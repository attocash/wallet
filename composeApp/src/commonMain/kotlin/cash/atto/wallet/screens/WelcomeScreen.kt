package cash.atto.wallet.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.logo
import attowallet.composeapp.generated.resources.welcome_create_wallet
import attowallet.composeapp.generated.resources.welcome_create_wallet_description
import attowallet.composeapp.generated.resources.welcome_import_wallet
import attowallet.composeapp.generated.resources.welcome_import_wallet_description
import attowallet.composeapp.generated.resources.welcome_message
import attowallet.composeapp.generated.resources.welcome_stats_confirmation
import attowallet.composeapp.generated.resources.welcome_stats_confirmation_description
import attowallet.composeapp.generated.resources.welcome_stats_confirmation_unit
import attowallet.composeapp.generated.resources.welcome_stats_market_cap
import attowallet.composeapp.generated.resources.welcome_stats_market_cap_description
import attowallet.composeapp.generated.resources.welcome_stats_price
import attowallet.composeapp.generated.resources.welcome_stats_price_description
import attowallet.composeapp.generated.resources.welcome_title
import cash.atto.wallet.components.common.AttoCard
import cash.atto.wallet.ui.attoFontFamily
import cash.atto.wallet.ui.dark_accent
import cash.atto.wallet.ui.dark_accent_soft
import cash.atto.wallet.ui.dark_accent_soft_hover
import cash.atto.wallet.ui.dark_bg
import cash.atto.wallet.ui.dark_border
import cash.atto.wallet.ui.dark_success
import cash.atto.wallet.ui.dark_surface
import cash.atto.wallet.ui.dark_surface_alt
import cash.atto.wallet.ui.dark_text_dim
import cash.atto.wallet.ui.dark_text_secondary
import cash.atto.wallet.ui.dark_text_tertiary
import cash.atto.wallet.ui.dark_violet
import cash.atto.wallet.ui.dark_violet_soft
import cash.atto.wallet.ui.dark_violet_soft_hover
import cash.atto.wallet.viewmodel.WelcomeViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private val WelcomePageBackground = dark_bg
private val WelcomeCardBackground = dark_surface
private val WelcomeCardBorder = dark_border
private val WelcomeCardHoverBackground = dark_surface_alt
private val WelcomeTitleColor = Color.White
private val WelcomeBodyColor = dark_text_secondary
private val WelcomeMutedColor = dark_text_tertiary
private val WelcomeStatCopyColor = dark_text_dim
private val WelcomeGold = dark_accent
private val WelcomeGoldSoft = dark_accent_soft
private val WelcomeGoldSoftHover = dark_accent_soft_hover
private val WelcomeViolet = dark_violet
private val WelcomeVioletSoft = dark_violet_soft
private val WelcomeVioletSoftHover = dark_violet_soft_hover
private val WelcomeGreen = dark_success

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun WelcomeScreen(
    onCreateSecretClicked: () -> Unit,
    onImportSecretClicked: () -> Unit,
) {
    val viewModel = koinViewModel<WelcomeViewModel>()
    val metrics by viewModel.state.collectAsState()
    val windowSizeClass = calculateWindowSizeClass()
    val compact = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact
    val scrollState = rememberScrollState()

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(WelcomePageBackground)
                .verticalScroll(scrollState)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    top = if (compact) 48.dp else 64.dp,
                    bottom = 40.dp,
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(max = 1024.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WelcomeHeader(compact = compact)
            if (compact) {
                Column(
                    modifier =
                        Modifier
                            .widthIn(max = 768.dp)
                            .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WelcomeActionCard(
                        title = stringResource(Res.string.welcome_create_wallet),
                        description = stringResource(Res.string.welcome_create_wallet_description),
                        iconBackground = WelcomeGoldSoft,
                        iconBackgroundHover = WelcomeGoldSoftHover,
                        onClick = onCreateSecretClicked,
                        icon = { WelcomeCreateIcon(WelcomeGold) },
                    )
                    WelcomeActionCard(
                        title = stringResource(Res.string.welcome_import_wallet),
                        description = stringResource(Res.string.welcome_import_wallet_description),
                        iconBackground = WelcomeVioletSoft,
                        iconBackgroundHover = WelcomeVioletSoftHover,
                        onClick = onImportSecretClicked,
                        icon = { WelcomeImportIcon(WelcomeViolet) },
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .widthIn(max = 768.dp)
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                ) {
                    WelcomeStatCard(
                        label = stringResource(Res.string.welcome_stats_market_cap),
                        value = metrics.marketCapValue,
                        description = stringResource(Res.string.welcome_stats_market_cap_description),
                        labelColor = WelcomeGold,
                        textAlign = TextAlign.Center,
                    )
                    WelcomeStatCard(
                        label = stringResource(Res.string.welcome_stats_confirmation),
                        value = metrics.confirmationValue,
                        valueSuffix = stringResource(Res.string.welcome_stats_confirmation_unit),
                        description = stringResource(Res.string.welcome_stats_confirmation_description),
                        labelColor = WelcomeViolet,
                        textAlign = TextAlign.Center,
                        monoValue = true,
                    )
                    WelcomeStatCard(
                        label = stringResource(Res.string.welcome_stats_price),
                        value = metrics.priceUsdValue,
                        description = stringResource(Res.string.welcome_stats_price_description),
                        labelColor = WelcomeGreen,
                        textAlign = TextAlign.Center,
                        monoValue = true,
                    )
                }
            } else {
                Row(
                    modifier =
                        Modifier
                            .widthIn(max = 768.dp)
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WelcomeActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.welcome_create_wallet),
                        description = stringResource(Res.string.welcome_create_wallet_description),
                        iconBackground = WelcomeGoldSoft,
                        iconBackgroundHover = WelcomeGoldSoftHover,
                        onClick = onCreateSecretClicked,
                        icon = { WelcomeCreateIcon(WelcomeGold) },
                    )
                    WelcomeActionCard(
                        modifier = Modifier.weight(1f),
                        title = stringResource(Res.string.welcome_import_wallet),
                        description = stringResource(Res.string.welcome_import_wallet_description),
                        iconBackground = WelcomeVioletSoft,
                        iconBackgroundHover = WelcomeVioletSoftHover,
                        onClick = onImportSecretClicked,
                        icon = { WelcomeImportIcon(WelcomeViolet) },
                    )
                }

                Row(
                    modifier =
                        Modifier
                            .widthIn(max = 768.dp)
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    WelcomeStatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.welcome_stats_market_cap),
                        value = metrics.marketCapValue,
                        description = stringResource(Res.string.welcome_stats_market_cap_description),
                        labelColor = WelcomeGold,
                        textAlign = TextAlign.Start,
                    )
                    WelcomeStatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.welcome_stats_confirmation),
                        value = metrics.confirmationValue,
                        valueSuffix = stringResource(Res.string.welcome_stats_confirmation_unit),
                        description = stringResource(Res.string.welcome_stats_confirmation_description),
                        labelColor = WelcomeViolet,
                        textAlign = TextAlign.Center,
                        monoValue = true,
                    )
                    WelcomeStatCard(
                        modifier = Modifier.weight(1f),
                        label = stringResource(Res.string.welcome_stats_price),
                        value = metrics.priceUsdValue,
                        description = stringResource(Res.string.welcome_stats_price_description),
                        labelColor = WelcomeGreen,
                        textAlign = TextAlign.End,
                        monoValue = true,
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeHeader(compact: Boolean) {
    Column(
        modifier = Modifier.padding(bottom = if (compact) 32.dp else 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.logo),
            contentDescription = "Atto Wallet",
            modifier = Modifier.size(64.dp),
        )

        Text(
            modifier = Modifier.padding(top = 24.dp),
            text = stringResource(Res.string.welcome_title),
            color = WelcomeTitleColor,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.W600,
                    fontSize = if (compact) 36.sp else 48.sp,
                    lineHeight = if (compact) 40.sp else 52.8.sp,
                    letterSpacing = (-0.96).sp,
                ),
        )

        Text(
            modifier =
                Modifier
                    .padding(top = 12.dp)
                    .padding(horizontal = 16.dp)
                    .widthIn(max = 672.dp),
            text = stringResource(Res.string.welcome_message),
            color = WelcomeBodyColor,
            textAlign = TextAlign.Center,
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.W400,
                    fontSize = if (compact) 16.sp else 18.sp,
                    lineHeight = if (compact) 25.6.sp else 28.8.sp,
                ),
        )
    }
}

@Composable
private fun WelcomeActionCard(
    title: String,
    description: String,
    iconBackground: Color,
    iconBackgroundHover: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    AttoCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 216.dp),
        background = WelcomeCardBackground,
        hoverBackground = WelcomeCardHoverBackground,
        border = WelcomeCardBorder,
        hoverBorder = WelcomeCardBorder,
        contentPadding = PaddingValues(24.dp),
        interactionSource = interactionSource,
        onClick = onClick,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
            verticalArrangement = Arrangement.Top,
        ) {
            Column(
                modifier = Modifier.padding(bottom = 32.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(48.dp)
                            .background(
                                color = if (isHovered) iconBackgroundHover else iconBackground,
                                shape = RoundedCornerShape(8.dp),
                            ),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(modifier = Modifier.size(24.dp)) {
                        icon()
                    }
                }
            }

            Text(
                text = title,
                color = if (isHovered) WelcomeGold else WelcomeTitleColor,
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.W600,
                        fontSize = 18.sp,
                        lineHeight = 21.6.sp,
                    ),
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = description,
                color = WelcomeMutedColor,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        lineHeight = 22.4.sp,
                    ),
            )
        }
    }
}

@Composable
private fun WelcomeStatCard(
    label: String,
    value: String,
    description: String,
    labelColor: Color,
    textAlign: TextAlign,
    modifier: Modifier = Modifier,
    valueSuffix: String? = null,
    monoValue: Boolean = false,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(20.dp),
        horizontalAlignment =
            when (textAlign) {
                TextAlign.Center -> Alignment.CenterHorizontally
                TextAlign.End -> Alignment.End
                else -> Alignment.Start
            },
    ) {
        Text(
            text = label.uppercase(),
            color = labelColor,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    letterSpacing = 1.98.sp,
                ),
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            horizontalArrangement =
                when (textAlign) {
                    TextAlign.Center -> Arrangement.Center
                    TextAlign.End -> Arrangement.End
                    else -> Arrangement.Start
                },
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = value,
                color = WelcomeTitleColor,
                textAlign = textAlign,
                modifier = Modifier.alignByBaseline(),
                style =
                    MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = if (monoValue) FontFamily.Monospace else attoFontFamily(),
                        fontWeight = FontWeight.W800,
                        fontSize = 32.sp,
                        lineHeight = 32.sp,
                    ),
            )

            if (valueSuffix != null) {
                Text(
                    modifier =
                        Modifier
                            .padding(start = 4.dp)
                            .alignByBaseline(),
                    text = valueSuffix,
                    color = WelcomeMutedColor,
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.W400,
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                        ),
                )
            }
        }

        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            text = description,
            color = WelcomeStatCopyColor,
            textAlign = textAlign,
            style =
                MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.W500,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                ),
        )
    }
}

@Composable
private fun WelcomeCreateIcon(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = size.minDimension * 0.1f
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.18f),
            end = Offset(size.width / 2f, size.height * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height / 2f),
            end = Offset(size.width * 0.82f, size.height / 2f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
private fun WelcomeImportIcon(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val strokeWidth = size.minDimension * 0.1f
        drawLine(
            color = color,
            start = Offset(size.width / 2f, size.height * 0.12f),
            end = Offset(size.width / 2f, size.height * 0.66f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.28f, size.height * 0.46f),
            end = Offset(size.width / 2f, size.height * 0.68f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.72f, size.height * 0.46f),
            end = Offset(size.width / 2f, size.height * 0.68f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(size.width * 0.18f, size.height * 0.82f),
            end = Offset(size.width * 0.82f, size.height * 0.82f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}
