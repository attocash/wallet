# Repository Guidelines

## Project Structure & Module Organization

Atto Wallet is a Kotlin/Compose Multiplatform app with one Gradle module, `:composeApp`. Wasm is the only stable target. Desktop works and is maintained on a best-effort basis. Android is unreleased: its code evolves, but we do not run or test it. iOS scaffolding is inactive.

Under `composeApp/src/`, `commonMain` holds shared UI, repositories, and domain logic; `wasmJsMain`, `desktopMain`, and `androidMain` contain platform implementations. Tests live in `commonTest`, `wasmJsTest`, and `androidInstrumentedTest`. Assets live in `commonMain/composeResources` and `wasmJsMain/resources`; Room schemas are in `composeApp/schemas/`.

## Build, Test, and Development Commands

Use JDK 17 and Node.js/npm for web development. Run from the repository root:

- `./gradlew wasmJsBrowserDevelopmentRun` — start the web development server.
- `./gradlew composeApp:run` — run desktop.
- `./gradlew :composeApp:compileKotlinWasmJs` — compile Wasm.
- `./gradlew wasmJsBrowserDistribution` — build the production web bundle.
- `./gradlew allTests` — run the main CI test task.
- `./gradlew :composeApp:wasmJsBrowserTest` — run browser tests, as web-build CI does.
- `./gradlew ktlintFormat` — format Kotlin sources.

Rerun a failing Wasm incremental compilation task once with `--rerun-tasks`. CI does not run ktlint; run `ktlintCheck` only when explicitly requested.

## Coding Style & Naming Conventions

Follow `.editorconfig`: four-space Kotlin/Kotlin DSL indentation, two spaces otherwise, LF endings, and 140-character code lines. Use official Kotlin style, PascalCase types/Composables, camelCase members, and explicit imports. Keep shared logic in `commonMain` and platform details behind existing `expect`/`actual` boundaries.

## Testing Guidelines

Shared and Wasm tests use `kotlin.test` and coroutine test utilities. Android instrumented tests use AndroidX/JUnit but are not run. Name files `*Test.kt` and use descriptive backtick test names. Add focused behavior tests in the appropriate source set. No coverage percentage is enforced. Report automated checks and manual browser validation separately.

## UI & Assets

Follow canonical `DESIGN.md`; reuse `components/common/Atto*` primitives. For Compose changes, use `.agents/skills/atto-components` and `.agents/skills/atto-style`; `DESIGN.md` wins conflicts. For branding, use `.agents/skills/logo-assets`, edit `composeApp/src/commonMain/composeResources/drawable/logo.svg`, and regenerate exports with `node scripts/generate-logo-assets.mjs`.

## Commit & Pull Request Guidelines

Use Conventional Commit prefixes matching scope and impact, such as `fix:`, `feat:`, or `chore(deps):`. Keep changes focused. Describe behavior changes, link relevant issues, record validation, and include screenshots for UI changes.

## Configuration Notes

DI selects `AttoNetwork.LIVE`. Production bundles land in `composeApp/build/dist/wasmJs/productionExecutable`; Firebase serves `artifacts/web`, populated by CI. Use `-Papp.version=...` for releases; otherwise app versions use the Git SHA and desktop packaging uses `0.0.0`.
