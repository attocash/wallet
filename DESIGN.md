---
version: alpha
name: Atto Wallet
description: Dark Compose Multiplatform design system extracted from the current wallet source.
colors:
  primary: "#FAB005"
  background: "#111113"
  surface: "#1A1A1D"
  surfaceAlt: "#222225"
  surfaceRaised: "#1C1C21"
  surfaceDeep: "#161618"
  fieldDeep: "#0F0F11"
  border: "#2C2C2F"
  borderSubtle: "#2A2A2F"
  borderMuted: "#232326"
  textPrimary: "#F5F5F5"
  textSecondary: "#A0A0A0"
  textTertiary: "#808080"
  textMuted: "#707070"
  textDim: "#606060"
  placeholder: "#505050"
  accent: "#FAB005"
  accentOn: "#111827"
  violet: "#7C3AED"
  success: "#10B981"
  danger: "#F87171"
  warning: "#FF9800"
  accountSky: "#38BDF8"
  accountAmber: "#F59E0B"
  qrBackground: "#FFFFFF"
typography:
  display-xl:
    fontFamily: Noto Sans
    fontSize: 80px
    fontWeight: 400
  display-lg:
    fontFamily: Noto Sans
    fontSize: 56px
    fontWeight: 600
  display-md:
    fontFamily: Noto Sans
    fontSize: 48px
    fontWeight: 600
    lineHeight: 1.1
  display-sm:
    fontFamily: Noto Sans
    fontSize: 42px
    fontWeight: 300
  headline-lg:
    fontFamily: Noto Sans
    fontSize: 34px
    fontWeight: 600
  headline-md:
    fontFamily: Noto Sans
    fontSize: 24px
    fontWeight: 600
  headline-sm:
    fontFamily: Noto Sans
    fontSize: 20px
    fontWeight: 400
  title-lg:
    fontFamily: Noto Sans
    fontSize: 18px
    fontWeight: 600
  title-md-tracked:
    fontFamily: Noto Sans
    fontSize: 14px
    fontWeight: 400
    letterSpacing: 2px
  body-lg:
    fontFamily: Noto Sans
    fontSize: 16px
    fontWeight: 400
  body-md:
    fontFamily: Noto Sans
    fontSize: 14px
    fontWeight: 400
  body-sm:
    fontFamily: Noto Sans
    fontSize: 12px
    fontWeight: 400
  label-lg:
    fontFamily: Noto Sans
    fontSize: 16px
    fontWeight: 600
  label-md:
    fontFamily: Noto Sans
    fontSize: 14px
    fontWeight: 400
  label-sm:
    fontFamily: Noto Sans
    fontSize: 12px
    fontWeight: 400
  label-caps:
    fontFamily: Noto Sans
    fontSize: 11px
    fontWeight: 700
    lineHeight: 16px
    letterSpacing: 1.98px
  button:
    fontFamily: Noto Sans
    fontSize: 15px
    fontWeight: 600
  button-strong:
    fontFamily: Noto Sans
    fontSize: 15px
    fontWeight: 700
  field-input:
    fontFamily: Noto Sans
    fontSize: 16px
    fontWeight: 500
  password-input:
    fontFamily: Noto Sans
    fontSize: 14px
    fontWeight: 400
  amount-input:
    fontFamily: Noto Sans
    fontSize: 32px
    fontWeight: 600
  data-lg:
    fontFamily: monospace
    fontSize: 32px
    fontWeight: 800
    lineHeight: 32px
  data-sm:
    fontFamily: monospace
    fontSize: 13px
    fontWeight: 600
  tag:
    fontFamily: Noto Sans
    fontSize: 14px
    fontWeight: 700
rounded:
  xs: 5px
  sm: 6px
  md: 8px
  lg: 10px
  xl: 12px
  xxl: 16px
  full: 999px
spacing:
  micro: 4px
  xs: 6px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 20px
  xxl: 24px
  xxxl: 32px
  huge: 48px
  shellMaxWidth: 1400px
  onboardingMaxWidth: 1024px
  onboardingContentMaxWidth: 768px
  loginMaxWidth: 480px
  passwordMaxWidth: 576px
  modalWidth: 520px
  compactBreakpoint: 600px
components:
  app-shell:
    backgroundColor: "{colors.background}"
    width: "{spacing.shellMaxWidth}"
    padding: "{spacing.xl}"
  top-bar:
    height: auto
    padding: "20px 32px"
    backgroundColor: "{colors.background}"
    textColor: "{colors.textPrimary}"
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "{colors.accentOn}"
    typography: "{typography.button-strong}"
    rounded: "{rounded.xl}"
    height: "56px"
    padding: "0 20px"
  button-secondary:
    backgroundColor: "alpha({colors.accent}, 0.12)"
    textColor: "{colors.textPrimary}"
    typography: "{typography.button}"
    rounded: "{rounded.xl}"
    height: "56px"
    padding: "0 20px"
  button-outlined:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.textPrimary}"
    typography: "{typography.button}"
    rounded: "{rounded.xl}"
    height: "56px"
    padding: "0 20px"
  button-danger:
    backgroundColor: "{colors.danger}"
    textColor: "{colors.background}"
    typography: "{typography.button-strong}"
    rounded: "{rounded.xl}"
    height: "56px"
    padding: "0 20px"
  card:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.textPrimary}"
    rounded: "{rounded.xl}"
    padding: "{spacing.xl}"
  modal:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.textPrimary}"
    rounded: "{rounded.xxl}"
    width: "{spacing.modalWidth}"
    padding: "{spacing.xl}"
  text-field:
    backgroundColor: "{colors.background}"
    textColor: "{colors.textPrimary}"
    typography: "{typography.field-input}"
    rounded: "{rounded.md}"
    padding: "{spacing.md}"
  password-field:
    backgroundColor: "{colors.surface}"
    textColor: "{colors.textPrimary}"
    typography: "{typography.password-input}"
    rounded: "{rounded.md}"
  amount-field:
    backgroundColor: "{colors.background}"
    textColor: "{colors.textPrimary}"
    typography: "{typography.amount-input}"
    rounded: "{rounded.md}"
  copy-field:
    backgroundColor: "{colors.fieldDeep}"
    textColor: "{colors.textPrimary}"
    typography: "{typography.data-sm}"
    rounded: "{rounded.md}"
    padding: "{spacing.md}"
  tag:
    backgroundColor: "alpha({colors.accent}, 0.12)"
    textColor: "{colors.accent}"
    typography: "{typography.tag}"
    rounded: "{rounded.sm}"
    padding: "4px 10px"
  status-success:
    backgroundColor: "alpha({colors.success}, 0.12)"
    textColor: "{colors.success}"
    typography: "{typography.tag}"
    rounded: "{rounded.sm}"
    padding: "4px 10px"
  status-danger:
    backgroundColor: "alpha({colors.danger}, 0.12)"
    textColor: "{colors.danger}"
    typography: "{typography.tag}"
    rounded: "{rounded.sm}"
    padding: "4px 10px"
  status-warning:
    backgroundColor: "alpha({colors.warning}, 0.12)"
    textColor: "{colors.warning}"
    typography: "{typography.tag}"
    rounded: "{rounded.sm}"
    padding: "4px 10px"
  status-violet:
    backgroundColor: "alpha({colors.violet}, 0.12)"
    textColor: "{colors.violet}"
    typography: "{typography.tag}"
    rounded: "{rounded.sm}"
    padding: "4px 10px"
  qr-panel:
    backgroundColor: "{colors.qrBackground}"
    rounded: "{rounded.xl}"
    padding: "{spacing.xxl}"
---

## Brand & Style

Atto Wallet is a dark, security-forward cryptocurrency wallet for the Atto network. The interface should feel fast, calm, precise, and operational rather than decorative. Product copy emphasizes speed, simplicity, local device security, fee-free transfers, live network status, and recovery responsibility.

The web/PWA build is the active product target, so responsive desktop and browser behavior should be treated as the primary experience. Desktop and Android remain present in the repository but are best-effort targets.

Use the gold Atto mark and gold accent as the first brand signal. The visual system is mostly neutral, with gold for primary actions and brand moments, violet for secondary accent moments, green for successful/healthy states, and red/orange only for error, danger, or warning.

## Colors

The palette is a dark neutral system with a single strong brand accent.

- **Background (`#111113`):** Window/page background and deepest app canvas.
- **Surface (`#1A1A1D`):** Primary card, input, modal, and panel surface.
- **Surface alt (`#222225`):** Hover and alternate raised surface.
- **Borders (`#2C2C2F`, `#2A2A2F`, `#232326`):** Thin separators and outlines, never heavy frames.
- **Text (`#F5F5F5`, `#A0A0A0`, `#808080`, `#707070`, `#606060`):** Clear foreground hierarchy from primary copy to metadata.
- **Primary/accent gold (`#FAB005`):** Brand mark, primary actions, cursor, active states, and important wallet status. The `primary` token satisfies the DESIGN.md schema role; `accent` mirrors the active Compose naming.
- **Violet (`#7C3AED`):** Secondary accent for import/representative or alternate informational states.
- **Success (`#10B981`):** Healthy, ready, complete, and positive network/account states.
- **Danger (`#F87171`):** Validation errors, destructive actions, and send/outgoing transaction emphasis.
- **Warning (`#FF9800`):** Staking warnings and state risk flags.
- **Account sky (`#38BDF8`) and account amber (`#F59E0B`):** Account identity dots only, as part of the account color rotation.
- **QR background (`#FFFFFF`):** QR code white backing only; do not use it for ordinary text.

Each semantic color has one base value. When a component needs a pressed, hover, disabled, surface, or border treatment, derive it from the base token with alpha or interpolation in code instead of introducing another frontmatter color. The main exception is foreground-on-fill colors such as `accentOn`, which define readable content on saturated action backgrounds.

## Typography

Use Noto Sans variable as the default UI family, with weights 400, 500, 600, 700, and 800 available. Compose `sp` values were converted to `px` in the frontmatter token set.

The system uses Material typography as a base, then applies screen-specific overrides for large wallet balance values, onboarding headers, login title treatment, uppercase metadata labels, button labels, amount fields, and transaction rows. Preserve the hierarchy:

- Large welcome and balance numbers can use 48-80px display sizes with tight line height.
- Page titles use 32-35px semi-bold type.
- Card titles use 15-20px semi-bold type. Button text is 15px, semi-bold for secondary/outlined actions and bold for primary/danger actions.
- Body copy generally sits at 14-16px.
- Metadata and tags use 10-13px text, often uppercase with positive tracking.
- Monospace is reserved for numeric live metrics and technical data where alignment or data feel matters.

Some active screens use negative tracking for hero and balance text. Because the DESIGN.md schema does not clearly define negative dimension support, those values are documented here instead of encoded as core typography tokens.

## Layout & Spacing

The app uses centered, bounded layouts over a full-window dark background.

The main shell caps content at 1400px, applies safe drawing insets, uses a top bar, then gives screen content 20px padding. The top bar uses 20px vertical padding, 32px desktop horizontal padding, and 20px compact horizontal padding. Compact width starts below 600px. Onboarding uses a 1024px outer column, 768px action/stat groups, 24px horizontal page padding, and larger vertical breathing room. Login is capped at 480px, while password creation is capped at 576px.

Spacing is built from repeated Compose `dp` values rather than a formal token file. Use 4px for micro gaps, 8px for field/card internal substructure, 12px for icon/text row gaps, 16px for related groups, 20px for default card and shell content padding, 24px for page sections and modal header/content padding, 32px for major onboarding/login breaks, and 48px for large welcome section separation.

## Elevation & Depth

Depth is tonal, not shadow-based. Cards, panels, fields, and modals sit on dark surfaces with 1px borders and small hover shifts. Avoid drop shadows unless a new platform-specific surface needs a clear modality cue that cannot be achieved by border, dimming, or tonal contrast.

Hover behavior should change surface color, border color, icon tint, or soft accent overlay. Use hand cursors for clickable web elements, matching the active Compose components.

## Shapes

The Material theme defines 8px small, 12px medium, and 16px large shapes. Active UI expands that into a practical scale:

- 5px: checkbox control.
- 6px: tags and small status chips.
- 8px: text fields, metric pills, small highlight boxes, and icon backgrounds.
- 10px: transaction/account icon wells.
- 12px: cards, panels, and buttons.
- 16px: modals and large login/onboarding icon containers.
- Full/circle: status dots and circular icon controls.

Keep the shape language modest and precise. Do not introduce pill-heavy styling for ordinary buttons or cards unless the component is already circular or status-like.

## Components

**App shell:** Full-screen `#111113` background, top bar with brand mark, title, status indicator, settings and lock controls. Main content is centered with max 1400px width and 20px padding.

**Page frame:** Uses a 24px vertical rhythm, optional back button, 32px semi-bold title, secondary subtitle, and responsive action stacking under compact widths.

**Buttons:** Primary buttons are gold, 56px tall, 12px radius, 20px horizontal padding, and bold 15px label text. Secondary and outlined buttons use semi-bold 15px labels. Secondary buttons use soft gold overlays with hover/active borders handled in code; outlined buttons use dark surfaces and borders; danger buttons use the shared danger token with dark text.

**Cards and panels:** Use `#1A1A1D`, 1px `#2C2C2F` borders, 12px radius, and 20px or 24px padding. Hoverable cards move to `#222225`.

**Fields:** Text fields use dark surface fill, 8px radius, 1px border, primary text, muted placeholder, and gold cursor. Standard text fields use 16px medium text, password fields use 14px regular text, amount fields can promote to 32px semi-bold text, and copy/detail fields use a deep field surface with 13px monospace values.

**Modals:** Fixed 520px desktop width, 16px radius, dark surface, 24px header padding, 20px body padding, 24px body spacing, horizontal divider, and a text close control.

**Tags and status:** Use 14px bold colored text with low-alpha background and border, 6px radius, and 10px horizontal padding. Green is healthy/ready, gold is active/waiting, violet is secondary accent, red/orange signal risk.

**Transaction and data rows:** Use compact card rows with a 40px icon well, 15-16px value text, 10-12px metadata, and uppercase tracked labels for status/type markers.

## Do's and Don'ts

- Do use the gold mark and gold accent sparingly but consistently for the primary brand/action path.
- Do keep surfaces flat, bordered, and dark; hierarchy comes from tone, spacing, and text weight.
- Do keep copy direct and security-aware, especially around recovery phrases, logout/wipe, and password flows.
- Do use responsive width caps and stack actions below 600px.
- Do reserve monospace for numeric metrics or technical values.
- Don't add bright gradients, large shadows, or light marketing panels.
- Don't use violet as the primary action color.
- Don't encode alpha overlay colors as hex frontmatter color tokens; document or implement them as state rules.
- Don't treat older local screen implementations as stronger evidence than shared active components unless the screen is still actively rendered.

## Extraction Notes

- References inspected: 32 active product design references plus 1 prior design artifact.
- References used in tokens or prose: 32 active product design references.
- Prior target artifacts compared but excluded from support counts: 1 (`DESIGN.md` before this refresh).
- Format references inspected and excluded from evidence count: 1.
- Low-confidence or inferred decisions: 4.

### Reference IDs

- R1: `/var/home/felipe/IdeaProjects/wallet/README.md` - brand or product note
- R2: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/composeResources/values/strings.xml` - brand or product note
- R3: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/Color.kt` - token file
- R4: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/Typography.kt` - token file
- R5: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/AttoWalletTheme.kt` - token file
- R6: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/Responsive.kt` - implementation file
- R7: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/composeResources/drawable/logo.svg` - visual reference
- R8: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/wasmJsMain/resources/logo.svg` - visual reference
- R9: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoWallet.kt` - implementation file
- R10: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoButton.kt` - implementation file
- R11: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoCard.kt` - implementation file
- R12: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoModal.kt` - implementation file
- R13: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoTextField.kt` - implementation file
- R14: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoPasswordField.kt` - implementation file
- R15: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/WelcomeScreen.kt` - implementation file
- R16: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/LoginScreen.kt` - implementation file
- R17: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/OverviewScreen.kt` - implementation file
- R18: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/SendScreen.kt` - implementation file
- R19: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/ReceiveScreen.kt` - implementation file
- R20: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/StakingScreen.kt` - implementation file
- R21: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/SettingsScreen.kt` - implementation file
- R22: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/RecoveryPhraseScreen.kt` - implementation file
- R23: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/ImportPhraseScreen.kt` - implementation file
- R24: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoTransactionCard.kt` - implementation file
- R25: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoTag.kt` - implementation file
- R26: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoCopyField.kt` - implementation file
- R27: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/CreatePasswordScreen.kt` - implementation file
- R28: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoAmountField.kt` - implementation file
- R29: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoScreenTitle.kt` - implementation file
- R30: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoBackButton.kt` - implementation file
- R31: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoCopyButton.kt` - implementation file
- R32: `/var/home/felipe/IdeaProjects/wallet/composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoTransactionDetailsDialog.kt` - implementation file
- P1: `/var/home/felipe/IdeaProjects/wallet/DESIGN.md` before this refresh - generated or prior design artifact

| Area | Supporting refs | Count | Confidence |
|---|---|---:|---|
| Brand & Style | R1, R2, R7, R8, R9, R15, R16 | 7 | high |
| Colors | R3, R5, R7, R8, R9, R10, R11, R12, R13, R14, R15, R16, R17, R20, R21, R23, R25, R26, R27, R28, R31, R32 | 22 | high |
| Typography | R4, R9, R10, R13, R14, R15, R16, R17, R24, R25, R26, R28, R29, R32 | 14 | high |
| Layout & Spacing | R1, R6, R9, R10, R11, R12, R15, R16, R17, R18, R19, R20, R27, R28, R29, R30 | 16 | high |
| Elevation & Depth | R5, R9, R10, R11, R12, R15, R20 | 7 | high |
| Shapes | R5, R9, R10, R11, R12, R13, R14, R15, R16, R24, R25, R27, R28, R29, R30, R32 | 16 | high |
| Components | R9, R10, R11, R12, R13, R14, R15, R16, R17, R18, R19, R20, R21, R24, R25, R26, R27, R28, R29, R30, R31, R32 | 22 | high |

### Conflicts and Inconsistencies

- Components.button typography: prior frontmatter pointed primary/secondary buttons at 16px `label-lg`, while `AttoButton` renders 15px labels with variant-specific 600/700 weights. Chosen 15px button tokens from R10.
- Components.top-bar padding: prior frontmatter encoded `32px 20px`, but the active top bar uses 20px vertical padding and 32px desktop horizontal padding, with 20px horizontal padding on compact widths. Chosen `20px 32px` from R9 and documented the compact exception in prose.
- Colors.primary: prior frontmatter had `accent` but no schema-facing `primary`. Chosen `primary: #FAB005` as an alias of the active Compose `dark_accent` from R3/R5/R7/R8.
- Components.tag padding: prior frontmatter used `4px 8px`, while `AttoTag` uses `4px 10px`. Chosen `4px 10px` from R25.

### Inferred Decisions

- Line-height conversions are inferred where Compose text styles omit explicit line height.
- The spacing scale is inferred from repeated `dp` values because no formal spacing token file exists.
- `full: 999px` is a practical alias for circular controls because the source uses `CircleShape`.
- Warning semantics are inferred from staking-specific risk usage and mapped to the shared `warning: #FF9800` token.
