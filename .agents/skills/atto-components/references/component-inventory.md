# Component Inventory

Shared reusable components live in `composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common`.

## Common `Atto*` components

- `AttoAmountField`: numeric amount entry/display with ATTO/USD mode switch and supporting text.
- `AttoBackButton`: wallet-styled back navigation control.
- `AttoButton`: primary shared action button with `Accent`, `Secondary`, `Outlined`, and `Danger` variants.
- `AttoCard`: reusable surfaced card container used for grouped content and clickable cards.
- `AttoPageFrame`: standard internal page frame with title, subtitle, optional back button, and optional actions.
- `AttoPanelCard`: standard dark panel surface for page sections.
- `AttoCopyButton`: wallet-styled copy action.
- `AttoCopyField`: display field with copy affordance.
- `AttoLoader`: loading indicator component.
- `AttoModal`: shared modal container.
- `AttoPasswordForm`: shared password-form scope; associates native web controls with one HTML form and reads live autofilled values on submit.
- `AttoPasswordField`: styled password entry; use inside `AttoPasswordForm`. Keep `name = "password"` for the password being submitted,
  `name = "confirm-password"` for confirmation, and `name = "current-password"` for the existing password during a change.
  Use `ContentType.Password` for the current password and `ContentType.NewPassword` for both new-password fields.
- `AttoPasswordStrengthMeter`: shared strength feedback for password creation and changes; supply the shared policy assessment.
- `AttoPasswordSubmitButton`: password-form action; use inside `AttoPasswordForm` so web submission follows the native form lifecycle.
- `AttoScreenTitle`: screen heading component.
- `AttoTextField`: shared text field wrapper with done handling and error slot.
- `AttoTransactionCard`: transaction summary row/card for overview and history screens.
- `AttoTransactionDetailsDialog`: transaction detail presentation dialog.
- `AttoWallet`: wallet summary/presentation component.
- `AttoWordChip`: mnemonic/recovery word chip.

## Related non-`Atto` shared components

- `QrCodeImage` and `QrScanner`: QR-specific UI.

## Theme references

- `DESIGN.md`: canonical design system and resolved visual conflicts.
- `evidence-matrix.md`: supporting evidence behind `DESIGN.md`.
- `composeApp/src/commonMain/kotlin/cash/atto/wallet/ui/AttoWalletTheme.kt`: theme entry point, color scheme, shapes.
- Search the same `ui` package for typography, colors, and formatters before introducing new visual tokens.

## Practical guidance

- Search call sites before changing a shared component API.
- Prefer adding parameters with safe defaults over forking a near-duplicate component.
- Keep previews wrapped in `AttoWalletTheme`.
- Password creation must use the shared strength policy for both the meter and submission. Keep password-manager generation hints
  separate from acceptance rules, and preserve existing-wallet unlock when changing creation policy.

## Native browser controls and dialogs

- When a Compose dialog obscures `HtmlElementView` controls, remove the interop views and preserve their layout with same-size spacers.
  Hiding only the native child leaves Compose's wrapper intercepting clicks over the dialog.
- Keep retained password bindings outside that visibility branch and dispose them when the owning composition ends, so dismissing a dialog
  restores live controls and their values.
- Verify actual mouse clicks and typing across modal fields on wide and compact viewports, then dismiss/reopen and check password input,
  reveal, Enter submission, and Escape. Programmatic focus alone misses intercepting wrappers.
