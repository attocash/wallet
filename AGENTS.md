# AGENTS

## Scope

- Treat the web/Wasm target as the source of truth. `README.md` and CI both treat `wasmJs` as the actively supported target; desktop and Android remain in the repo but are not regularly validated.
- The repo has one Gradle module: `:composeApp`. Most app work lives under `composeApp/src/commonMain`; platform-specific code is in `androidMain`, `desktopMain`, and `wasmJsMain`.

## Commands

- Run the main CI-equivalent verification with `./gradlew allTests`.
- Run formatting with `./gradlew ktlintFormat`. Do not run `./gradlew ktlintCheck` unless explicitly requested; CI does not run ktlint for you.
- Start the main dev target with `./gradlew wasmJsBrowserDevelopmentRun`.
- Build the production web artifact with `./gradlew wasmJsBrowserDistribution`.
- Run the desktop app with `./gradlew composeApp:run`.
- Install the Android debug build with `./gradlew composeApp:installDebug`.
- For a focused Wasm compile check, use `./gradlew :composeApp:compileKotlinWasmJs`.

## Verified Gotchas

- If Kotlin/Wasm incremental compilation flakes, rerun the failing Wasm task once with `--rerun-tasks`; `README.md` calls this out explicitly for `wasmJsBrowserDevelopmentRun` and `:composeApp:compileKotlinWasmJs`.
- Web builds require Node/npm because `composeApp/sqlite-web-worker/package.json` provides the local `sqlite-web-worker` npm dependency used by `wasmJsMain`.
- Firebase hosting serves `artifacts/web` (`firebase.json`), but Gradle outputs the web bundle to `composeApp/build/dist/wasmJs/productionExecutable`; CI bridges that mismatch by downloading the uploaded web artifact into `artifacts/` before deploy.
- Builds use `app.version` when passed explicitly with `-Papp.version=...`; otherwise `composeApp/build.gradle.kts` falls back to `git rev-parse --short HEAD` for app-facing unreleased versioning. Desktop package metadata still needs a valid numeric version, so unreleased packaging falls back to `0.0.0`.
- The app is hard-wired to `AttoNetwork.LIVE` in `composeApp/src/commonMain/kotlin/cash/atto/wallet/di/Modules.kt`.

## Architecture

- Real web entrypoint: `composeApp/src/wasmJsMain/kotlin/cash/atto/wallet/Main.kt`.
- Shared app shell/navigation: `composeApp/src/commonMain/kotlin/cash/atto/wallet/AttoNavHost.kt`, `DWNavigation.kt`, and `AttoDestination.kt`.
- Shared DI starts from `composeApp/src/commonMain/kotlin/cash/atto/wallet/di/Modules.kt` and `Koin.kt`; platform storage/database bindings come from `expect`/`actual` modules (`databaseModule`, `dataSourceModule`).
- Repositories called out by the repo docs and DI are the main data flow boundaries: `AppStateRepository`, `HomeRepository`, `MetricsRepository`, `VotersRepository`, and `WalletManagerRepository`.

## UI Conventions

- Treat `DESIGN.md` as the canonical product design system for colors, typography, spacing, shape scale, component tokens, and resolved design conflicts. `evidence-matrix.md` records the evidence and conflict decisions behind that system.
- Reuse shared Compose primitives under `composeApp/src/commonMain/kotlin/cash/atto/wallet/components/common` before adding new screen-local widgets. The repo already ships wallet-specific wrappers such as `AttoButton`, `AttoTextField`, `AttoCard`, `AttoModal`, and `AttoTransactionCard`.
- For Compose UI changes, use `.agents/skills/atto-components` for component reuse workflow and `.agents/skills/atto-style` for implementation workflow. If either skill conflicts with `DESIGN.md`, `DESIGN.md` wins and the skill should be updated.

## Testing Reality

- CI runs only `./gradlew allTests` on Linux/macOS/Windows plus packaging/web build workflows. It does not run ktlint.
- Checked-in test coverage is currently sparse. The only obvious test source in the repo is Android instrumented: `composeApp/src/androidInstrumentedTest/kotlin/cash/atto/wallet/PasswordDataSourceTest.kt`. Do not assume broad automated coverage across targets.
