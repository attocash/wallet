package cash.atto.wallet.components.common

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import attowallet.composeapp.generated.resources.Res
import attowallet.composeapp.generated.resources.logo
import cash.atto.wallet.MainScreenNavDestination
import cash.atto.wallet.ui.*
import cash.atto.wallet.uistate.desktop.BalanceChipUiState
import cash.atto.wallet.uistate.overview.TransactionUiState
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import org.jetbrains.compose.resources.painterResource

val LogoutShellIcon: ImageVector
    get() = Icons.AutoMirrored.Outlined.Logout

@Composable
fun AttoWallet(
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    balanceUiState: BalanceChipUiState,
    isWalletInitialized: Boolean,
    hasCachedWork: Boolean,
    onLock: () -> Unit,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(dark_bg),
    ) {
        BoxWithConstraints(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(WindowInsets.safeDrawing.asPaddingValues()),
        ) {
            val shellMaxWidth = maxWidth
            val compact = shellMaxWidth.isCompactWidth()
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                AttoTopBar(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    compact = compact,
                    navState = navState,
                    onNavStateChanged = onNavStateChanged,
                    isWalletInitialized = isWalletInitialized,
                    hasCachedWork = hasCachedWork,
                    priceUsd = balanceUiState.priceUsd,
                    onLock = onLock,
                )
                HorizontalDivider(color = dark_border)

                Box(
                    modifier =
                        Modifier
                            .fillMaxSize(),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .widthIn(max = 1400.dp)
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .align(Alignment.TopCenter)
                                .padding(all = 20.dp),
                    ) {
                        this@BoxWithConstraints.content()
                    }
                }
            }
        }
    }
}

@Composable
private fun AttoTopBar(
    modifier: Modifier = Modifier,
    compact: Boolean,
    navState: MainScreenNavDestination,
    onNavStateChanged: (MainScreenNavDestination) -> Unit,
    isWalletInitialized: Boolean,
    hasCachedWork: Boolean,
    priceUsd: BigDecimal?,
    onLock: () -> Unit,
) {
    val showCachedWorkInfo = rememberSaveable { mutableStateOf(false) }
    val showTradeDialog = rememberSaveable { mutableStateOf(false) }
    val availablePriceUsd = priceUsd?.takeIf { it > BigDecimal.ZERO }

    Row(
        modifier =
            modifier
                .widthIn(max = 1400.dp)
                .fillMaxWidth()
                .padding(horizontal = if (compact) 20.dp else 32.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AttoShellBrandMark()
            if (!compact) {
                Text(
                    text = "Atto Wallet",
                    color = dark_text_primary,
                    maxLines = 1,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.W600,
                            fontSize = 20.sp,
                        ),
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isWalletInitialized) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    availablePriceUsd?.let {
                        AttoShellPriceChip(
                            priceUsd = it,
                            compact = compact,
                            onClick = { showTradeDialog.value = true },
                        )
                    }
                    AttoShellStatusIndicator(
                        compact = compact,
                        hasCachedWork = hasCachedWork,
                        onClick = { showCachedWorkInfo.value = true },
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AttoCircleIconButton(
                    icon = Icons.Outlined.Settings,
                    contentDescription = "Open settings",
                    tint = if (navState == MainScreenNavDestination.SETTINGS) dark_accent else dark_text_secondary,
                    background = Color.Transparent,
                    onClick = { onNavStateChanged(MainScreenNavDestination.SETTINGS) },
                )
                AttoCircleIconButton(
                    icon = Icons.Outlined.Lock,
                    contentDescription = "Lock wallet",
                    tint = dark_text_secondary,
                    background = Color.Transparent,
                    onClick = onLock,
                )
            }
        }
    }

    if (showCachedWorkInfo.value) {
        CachedWorkInfoDialog(
            hasCachedWork = hasCachedWork,
            onDismiss = { showCachedWorkInfo.value = false },
        )
    }

    if (showTradeDialog.value && availablePriceUsd != null) {
        AttoTradeDialog(
            priceUsd = availablePriceUsd,
            onDismiss = { showTradeDialog.value = false },
        )
    }
}

@Composable
private fun AttoShellBrandMark() {
    Image(
        painter = painterResource(Res.drawable.logo),
        contentDescription = "Atto Wallet",
        modifier =
            Modifier
                .size(32.dp),
    )
}

@Composable
private fun AttoShellStatusIndicator(
    compact: Boolean,
    hasCachedWork: Boolean,
    onClick: () -> Unit,
) {
    val statusColor = if (hasCachedWork) dark_success else dark_accent
    Box(
        modifier =
            Modifier
                .size(28.dp)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size(if (compact) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(statusColor),
        )
    }
}

@Composable
private fun AttoShellPriceChip(
    priceUsd: BigDecimal,
    compact: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Row(
        modifier =
            Modifier
                .height(28.dp)
                .clip(shape)
                .background(if (hovered) dark_surface_alt else dark_surface)
                .border(1.dp, dark_border, shape)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).padding(horizontal = if (compact) 8.dp else 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
            contentDescription = null,
            tint = attoHoverTint(dark_success, hovered, highlight = 0.18f),
            modifier = Modifier.size(14.dp),
        )
        if (!compact) {
            Text(
                text = "ATTO",
                color = dark_text_tertiary,
                maxLines = 1,
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W700,
                        fontSize = 11.sp,
                    ),
            )
        }
        Text(
            text = "$${AttoFormatter.format(priceUsd)}",
            color = dark_text_primary,
            maxLines = 1,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.W700,
                    fontSize = 12.sp,
                ),
        )
    }
}

@Composable
private fun AttoTradeDialog(
    priceUsd: BigDecimal,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    AttoModal(
        title = "Buy or sell ATTO",
        onDismiss = onDismiss,
    ) {
        Text(
            text =
                "Want to buy or sell? These links open third-party exchanges. Atto Wallet " +
                    "does not operate these markets, provide trading advice, or control pricing, " +
                    "fees, liquidity, or regional availability. Review the exchange details and " +
                    "risks before trading.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(dark_success.copy(alpha = 0.04f))
                    .border(1.dp, dark_success.copy(alpha = 0.18f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                contentDescription = null,
                tint = dark_success,
                modifier = Modifier.size(18.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "Current ATTO price",
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "$${AttoFormatter.format(priceUsd)} USD",
                    color = dark_text_primary,
                    style =
                        MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.W700,
                        ),
                )
            }
        }

        AttoTradeExchangeOption(
            title = "XT",
            pair = "ATTO/USDT",
            description =
                "Global digital asset exchange with spot, margin, derivatives, and OTC trading across a large range of markets.",
            onClick = { uriHandler.openUri("https://www.xt.com/en/trade/atto_usdt") },
        )

        AttoTradeExchangeOption(
            title = "LCX",
            pair = "ATTO/EUR",
            description =
                "Liechtenstein-regulated digital asset exchange focused on compliant trading and tokenization.",
            onClick = { uriHandler.openUri("https://lcx.com/en/trade/ATTO-EUR") },
        )
    }
}

@Composable
private fun AttoTradeExchangeOption(
    title: String,
    pair: String,
    description: String,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(8.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(if (hovered) dark_surface_alt else dark_surface_alt.copy(alpha = 0.42f))
                .border(1.dp, dark_border, shape)
                .pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    color = dark_text_primary,
                    style =
                        MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.W700,
                        ),
                )
                Text(
                    text = description,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Text(
                text = pair,
                color = dark_accent,
                maxLines = 1,
                modifier =
                    Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(dark_accent_soft)
                        .border(1.dp, dark_accent.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.W700,
                    ),
            )
        }
    }
}

@Composable
private fun CachedWorkInfoDialog(
    hasCachedWork: Boolean,
    onDismiss: () -> Unit,
) {
    val highlightColor = if (hasCachedWork) dark_success else dark_accent
    val highlightIcon = if (hasCachedWork) Icons.Outlined.CheckCircle else Icons.Outlined.WarningAmber
    val highlightTitle = if (hasCachedWork) "Work ready" else "Preparing work"
    val highlightDescription =
        if (hasCachedWork) {
            "Your wallet has already prepared the required work, so your next transaction can be sent immediately."
        } else {
            "Your wallet is preparing the required work in the background. Once it is ready, your next transaction can be sent immediately."
        }

    AttoModal(
        title = "About spam protection",
        onDismiss = onDismiss,
    ) {
        Text(
            text =
                "Before a transaction is sent, your wallet prepares a tiny amount of work. " +
                    "This helps protect the network from spam. It does not cost any ATTO " +
                    "and can be prepared ahead of time.",
            color = dark_text_secondary,
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(highlightColor.copy(alpha = 0.03f))
                    .border(1.dp, highlightColor.copy(alpha = 0.19f), RoundedCornerShape(8.dp))
                    .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = highlightIcon,
                contentDescription = null,
                tint = highlightColor,
                modifier = Modifier.size(18.dp),
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = highlightTitle,
                    color = highlightColor,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.W700),
                )
                Text(
                    text = highlightDescription,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun AttoPageFrame(
    title: String,
    subtitle: String,
    onBack: (() -> Unit)? = null,
    scrollable: Boolean = true,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        onBack?.let {
            AttoBackButton(onClick = it)
        }

        val contentModifier =
            Modifier
                .weight(1f)
                .fillMaxWidth()

        val layoutModifier =
            if (scrollable) {
                contentModifier.verticalScroll(rememberScrollState())
            } else {
                contentModifier
            }

        Column(
            modifier = layoutModifier,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val stackActions = isCompactWidth()

                if (stackActions) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = title,
                                color = dark_text_primary,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.W600,
                                        fontSize = 32.sp,
                                    ),
                            )
                            Text(
                                text = subtitle,
                                color = dark_text_secondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        actions?.let { actionContent ->
                            Row(
                                modifier = Modifier.align(Alignment.End),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = actionContent,
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = title,
                                color = dark_text_primary,
                                style =
                                    MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.W600,
                                        fontSize = 32.sp,
                                    ),
                            )
                            Text(
                                text = subtitle,
                                color = dark_text_secondary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        actions?.let { actionContent ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                content = actionContent,
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}

@Composable
fun AttoPanelCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier =
            modifier
                .clip(RoundedCornerShape(12.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(12.dp))
                .padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content,
    )
}

@Composable
fun AttoMetricPill(
    label: String,
    value: String,
) {
    Column(
        modifier =
            Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(dark_surface)
                .border(1.dp, dark_border, RoundedCornerShape(8.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = label,
            color = dark_text_secondary,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = value,
            color = dark_text_primary,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.W600),
        )
    }
}

@Composable
fun AttoCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color = dark_text_primary,
    background: Color = dark_surface_alt.copy(alpha = 0.4f),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val transparentBackground = background == Color.Transparent
    val buttonSize = if (transparentBackground) 24.dp else 30.dp
    val iconSize = if (transparentBackground) 20.dp else buttonSize * 2 / 3

    Box(
        modifier =
            Modifier
                .size(buttonSize)
                .then(
                    if (transparentBackground) {
                        Modifier
                    } else {
                        Modifier
                            .clip(CircleShape)
                            .background(background)
                            .border(
                                width = 1.dp,
                                color = dark_border,
                                shape = CircleShape,
                            )
                    },
                ).pointerHoverIcon(PointerIcon.Hand)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = attoHoverTint(tint, hovered),
            modifier = Modifier.size(iconSize),
        )
    }
}

@Composable
fun AttoTransactionList(
    transactions: List<TransactionUiState?>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
) {
    val items = transactions.filterNotNull()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (items.isEmpty()) {
            AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = emptyMessage,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { transaction ->
                    AttoTransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
fun AttoTransactionSection(
    title: String,
    transactions: List<TransactionUiState?>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
    onTransactionClick: ((TransactionUiState) -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            color = dark_text_primary,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.W600),
        )
        AttoTransactionList(
            transactions = transactions,
            modifier = Modifier.fillMaxWidth(),
            emptyMessage = emptyMessage,
            onTransactionClick = onTransactionClick,
        )
    }
}

@Composable
fun AttoTransactionList(
    transactions: List<TransactionUiState?>,
    modifier: Modifier = Modifier,
    emptyMessage: String,
    onTransactionClick: ((TransactionUiState) -> Unit)?,
) {
    val items = transactions.filterNotNull()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (items.isEmpty()) {
            AttoPanelCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = emptyMessage,
                    color = dark_text_secondary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { transaction ->
                    AttoTransactionCard(
                        transaction = transaction,
                        onClick = onTransactionClick?.let { { it(transaction) } },
                    )
                }
            }
        }
    }
}

@Composable
fun AttoSettingsInfoCard(
    title: String,
    lines: List<String>,
    modifier: Modifier = Modifier,
) {
    AttoPanelCard(modifier = modifier) {
        Text(
            text = title,
            color = dark_text_primary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W600),
        )
        HorizontalDivider(color = dark_border)
        lines.forEach { line ->
            Text(
                text = line,
                color = dark_text_secondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
