# Atto Style Guide

This is a quick implementation reference. `DESIGN.md` is canonical for tokens, typography, spacing, shapes, and conflict decisions. If this file conflicts with `DESIGN.md`, follow `DESIGN.md` and update this file.

Source screens reviewed:

- `composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/OverviewScreen.kt`
- `composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/WelcomeScreen.kt`
- `composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/ReceiveScreen.kt`
- `composeApp/src/commonMain/kotlin/cash/atto/wallet/screens/SendScreen.kt`

Core shared primitives:

- `composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common/AttoWallet.kt`
- `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/Color.kt`
- `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/Typography.kt`

## Visual Direction

- Dark-first interface with premium financial-product tone.
- Surfaces are layered by value shifts, not by large shadow effects.
- Accent colors are sparse and intentional.
- The UI feels precise and calm, not playful or soft.

## Palette

- Background: `dark_bg`
- Main surface: `dark_surface`
- Hover or alternate surface: `dark_surface_alt`
- Border: `dark_border`
- Primary text: `dark_text_primary`
- Secondary text: `dark_text_secondary`
- Metadata text: `dark_text_tertiary`, `dark_text_muted`, `dark_text_dim`
- Brand accent: `dark_accent`
- Secondary accent: `dark_violet`
- Positive state: `dark_success`
- Danger state: `dark_danger`

## Container Language

- Most reusable panels are dark rectangles with `12.dp` radius, `1.dp` border, and `20.dp` to `24.dp` padding.
- Repeated panel rhythm matters more than one-off decorative containers.
- `AttoPanelCard` is the clearest current panel reference.
- `AttoPageFrame` is the standard structure for internal screens with title, subtitle, optional back button, and optional action row.

## Spacing Rhythm

- Common outer spacing: `24.dp`
- Common panel gap: `16.dp` or `24.dp`
- Common section gap on landing screens: `32.dp` to `48.dp`
- Common card padding: `20.dp` or `24.dp`

## Typography

- Typeface: `attoFontFamily()` based on Noto Sans variable.
- Heading style: semibold to bold, often `W600` or heavier.
- Large numeric values often use stronger weight and tighter line height.
- Eyebrow and stat labels often use uppercase plus extra letter spacing.
- Use monospace selectively for metrics or machine-like values.

## Screen-Specific Notes

### Welcome

- Centered composition.
- Large logo, large title, muted supporting copy.
- Action cards are tall, minimal, and hover-reactive.
- Stats sit below actions and use colored uppercase labels with strong numeric values.

### Overview

- Dashboard structure.
- Main balance card first.
- Dense but controlled information hierarchy.
- Accent color maps to account and transaction semantics.
- Right column focuses on transactions and supporting activity.

### Receive

- Utility-first layout.
- QR code lives inside a bright white inset within a dark panel.
- Request amount and address presentation stay simple and centered.
- Activity column mirrors the same panel language as the send flow.

### Send

- Transaction flow broken into discrete panels.
- Amount, address, balance, fee, and action areas are clearly separated.
- Uses a practical layout, not a decorative one.
- Action chips like `25%`, `50%`, `75%`, `MAX` use secondary button styling.

## Do

- Reuse existing tokens and shared wrappers.
- Keep contrast high and layout clean.
- Use accent colors to mark important actions, categories, and status.
- Preserve responsive behavior by stacking wide-screen sections vertically on compact screens.

## Avoid

- Bright full-screen gradients.
- Light backgrounds for whole screens.
- Excessively rounded shapes beyond current panel language.
- Heavy shadows, blur-heavy glass effects, or neon styling.
- Introducing a new visual motif per screen.
