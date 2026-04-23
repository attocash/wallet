---
name: atto-components
description: Reuse and extend this wallet project's Jetpack Compose UI components, especially shared `Atto*` components under `composeApp/src/commonMain/kotlin/cash/atto/wallet/components`. Use when editing screens, forms, cards, dialogs, onboarding flows, transaction UI, or other Compose UI in this repo so new work matches existing Atto patterns and does not create duplicate primitives.
---

# Atto Components

Use shared Compose components first. Match theme, spacing, typography, and interaction patterns so new UI stays consistent across platforms.

## Workflow

1. Check `composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common` for an existing `Atto*` component that already solves most of the problem.
2. Check `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui` for theme tokens, typography, colors, and helper types used by those components.
3. Reuse the existing component directly when the behavior fits.
4. Extend an existing `Atto*` component when you only need a small API change.
5. Create a new component only when no close fit exists. Put it with the other shared components if it is reusable.

## Rules

- Prefer `composeApp/src/commonMain` for reusable UI. Use platform-specific component directories only when the implementation truly depends on Android, Desktop, or Wasm APIs.
- Prefer `AttoButton`, `AttoTextField`, `AttoCard`, `AttoModal`, and other shared wrappers over raw Material components when they cover the use case.
- Prefer direct use of shared interactive components before adding screen-level wrappers.
- When a shared component already supports `onClick`, `enabled`, hover, or other interaction behavior, use that API instead of wrapping it in extra `clickable` layers or custom containers.
- For clickable UI on web and desktop, add `Modifier.pointerHoverIcon(PointerIcon.Hand)` on the actual interactive component layer. Prefer fixing shared `Atto*` components first so screens inherit the cursor behavior instead of repeating it locally.
- If a shared component looks wrong in a valid screen usage, fix the shared component baseline first instead of patching one screen.
- Avoid adding new customization parameters to shared components unless there is a repeated, real product need.
- Shared reusable components should keep same look and behavior across screens. Do not create screen-specific variants when shared component should stay consistent.
- For transaction UI, prefer `AttoTransactionCard` directly. Keep one visual treatment and one interaction pattern across overview, transactions, send, receive, and result screens.
- Wrap previews in `AttoWalletTheme`.
- Keep existing naming: reusable wallet-specific components start with `Atto`.
- When adding a new reusable component, keep its API small and align parameter names with nearby components.
- Avoid new third-party UI libraries for patterns the project already implements.

## Search Targets

- Shared components: `composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common`
- Theme and tokens: `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui`
- Existing screen usage: search `composeApp/src/commonMain/kotlin/cash/atto/wallet` for the component name before changing its API.

## Decision Heuristics

- Need a primary action, destructive action, or outlined action: start with `AttoButton`.
- Need text entry or password entry: start with `AttoTextField` or `AttoPasswordField`.
- Need amount entry or amount display: start with `AttoAmountInputField` or `AttoAmountField`.
- Need wallet transaction presentation: start with `AttoTransactionCard` and `AttoTransactionDetailsDialog`.
- Need framed content or tappable container: start with `AttoCard`.
- Need onboarding or modal structure: start with `AttoOnboardingContainer` or `AttoModal`.

## Reference

Read [references/component-inventory.md](references/component-inventory.md) when you need the current list of reusable shared components and their roles.
