---
name: atto-style
description: Match this wallet project's Atto visual style for Jetpack Compose screens and shared UI. Use when creating or editing Compose screens, cards, panels, onboarding flows, transaction pages, or responsive layouts in this repo so new work follows same dark premium look, spacing rhythm, typography, and restrained accent use.
---

# Atto Style

Use existing Atto look. Keep screens dark, calm, premium, structured. Gold and violet accent only. Not whole-screen fill.

## Workflow

1. Check target screen first.
2. Check nearby shared components.
3. Check `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui` for colors, type, responsive helpers.
4. Use `AttoPageFrame`, `AttoPanelCard`, `AttoButton`, `AttoCard`, other shared wrappers before custom containers.
5. Keep one strong header, few panels, clear spacing.
6. Adapt same screen for compact and wide layouts. Do not make two unrelated designs.

## Style Rules

- Use `dark_bg` for page background.
- Use `dark_surface` for cards and panels.
- Use `dark_surface_alt` for hover or raised state.
- Use `dark_border` for panel outlines and separators.
- Use `dark_text_primary` for main content.
- Use `dark_text_secondary` for supporting copy.
- Use dimmer text tokens for metadata.
- Use `dark_accent` as main brand accent.
- Use `dark_violet` or `dark_success` only for secondary emphasis, status, or category split.
- Keep corners rounded, not soft-heavy. Most panels use `RoundedCornerShape(12.dp)`. Small icon chips use `8.dp`.
- Keep panels airy. Common inner padding: `20.dp` or `24.dp`.
- Keep layout restrained. No heavy gradients, big shadows, or large illustration unless existing screen already does that.

## Layout Rules

- Use centered content column with max width for full-screen pages.
- Use `AttoPageFrame` for inner product pages like send, receive, settings, staking, and transaction views.
- On wide screens, prefer two-column layout: fixed-width primary panel plus flexible secondary panel.
- On compact screens, stack same sections in same order.
- Keep spacing steady. Common gaps: `16.dp`, `24.dp`, `32.dp`.

## Typography Rules

- Use `attoFontFamily()` through `MaterialTheme.typography`.
- Titles bold, tight. Main page titles often use `headlineMedium` or custom `headlineLarge` with heavier weight and slightly negative letter spacing.
- Labels and overlines small, uppercase, tracked out when emphasis needed.
- Supporting copy plain, readable, slightly muted.
- Use monospace only for machine-like values: heights, confirmation timing, some metrics.

## Interaction Rules

- Hover subtle. Usually `dark_surface` -> `dark_surface_alt`, or soft accent fill -> slightly stronger soft accent fill.
- Buttons and icon containers feel firm, not glossy.
- Accent color shows affordance. Not constant decoration.
- Empty states stay calm, informative, inside same card language as populated state.

## Common Patterns

- Landing and onboarding pages use centered composition, strong title, muted support copy, and a small number of action cards.
- Product pages use dark bordered panels, clear section splits, and compact action areas.
- Dashboard pages put key balance or status first, then actions, then activity.
- Utility pages like send and receive use left-right split on wide screens and vertical stacking on compact screens.

## Reference

Read [references/style-guide.md](references/style-guide.md) for detailed rules pulled from current screens.
