---
name: "logo-assets"
version: "1.0.1"
description: "Use when changing or auditing this wallet's brand SVG, logo symmetry, installed PWA icon padding, platform icon exports, or unused logo assets. Covers regeneration and cross-platform references. Excludes ordinary Compose styling, component reuse, design-system extraction, and creating a new brand with image generation."
license: "BSD-3-Clause"
compatibility: "Codex; Node.js and ImageMagick with the librsvg delegate for asset generation"
metadata:
  scope: "wallet repository"
  workflow: "brand-asset-maintenance"
---

# Logo assets

Read `AGENTS.md` for current checks, `README.md` for the logo/social-preview workflow, and `DESIGN.md` for colors. Run commands from the repository root.

## Inspect and change

1. Establish the affected surface: installation through Chrome uses the PWA manifest; an Android APK uses native launcher resources. Trace the actual manifest, HTML, Compose accessor, packaging configuration, or asset catalog involved.
2. Edit the canonical `composeApp/src/commonMain/composeResources/drawable/logo.svg`. Preserve its centered square viewBox and symmetry unless the requested design changes them. Audit quarter-turn rotations and horizontal, vertical, and diagonal reflections about the viewBox center; distinguish vector geometry from raster antialias differences.
3. Keep both `href` and matching `xlink:href` on internal SVG `<use>` references, with the `xmlns:xlink` declaration. Browsers accept `href`, while Skia SVG rendering can require `xlink:href`. Preserve internal IDs, clipping, and transforms. Compilation alone does not prove the logo renders in Compose.
4. Run `node scripts/generate-logo-assets.mjs`. It requires Node.js and ImageMagick with librsvg (`magick -list format` must include RSVG). If unavailable, use an isolated workbench or report the missing prerequisite; do not silently substitute a renderer that drops SVG references. Inspect the generator's current outputs rather than hand-editing generated web, desktop, Android, or iOS copies. Keep checked-in copies portable, without filesystem symlinks. If source changes invalidate generator assumptions, review those assumptions before exporting.
5. Regenerate `og-image.png` through the Compose route using the README workflow and `scripts/generate-og-image.mjs` after shared-logo changes. It embeds the logo and is not refreshed by the logo exporter.

## Padding and references

- Regular icons retain transparent bounds. PWA maskable icons have an opaque full background and separate padding: important artwork must fit inside the **centered circle with diameter 80% of canvas width**, not merely an 80% square. The generator's tighter layout leaves margin inside that circle. Preserve the center when changing the viewBox. Native Android's 108dp layer and 66dp safe region are a different specification.
- Declare regular SVG (`sizes: "any"`, `type: "image/svg+xml"`, `purpose: "any"`) separately from padded SVG (`purpose: "maskable"`). Retain 192px and 512px PNG entries for both purposes. Do not label an unpadded transparent asset `maskable` or combine purposes merely to reuse an export. Entry order does not guarantee SVG selection.
- Check `manifest.webmanifest`, HTML favicon/Apple touch references, and the service worker's `APP_SHELL` when adding or renaming files. Every precache URL must exist in the production artifact or worker installation can fail. Inspect current hosting cache headers before overwriting stable asset URLs; prefer a new filename for a new variant.
- Preserve stable-logo revalidation in Firebase Hosting and the shared Compose SVG's precache entry. A new worker cache name does not invalidate an older immutable HTTP-cache image: the shell's reload requests prevent that stale copy from entering the new cache. Verify this boundary with a warmed old image when changing cache behavior.
- Before deleting an apparently unused asset, trace Android resource qualifiers and manifest resolution, desktop package icon settings, iOS `Contents.json` catalogs and project references, generated accessors, HTML, manifest, and precache references. An exact-filename text search alone is insufficient. Retained vector alternatives may be intentional; establish their role before removal.

## Verify and report

- Parse manifest/SVG syntax; confirm paths, declared dimensions, opacity, safe-circle containment, and regular/maskable separation. Review regeneration output for stale variants and unrelated changes.
- Run relevant checks from `AGENTS.md`, including a production web build for packaging changes. Inspect the emitted manifest and referenced icons in the actual distribution. Kotlin compilation or tests alone do not prove resource packaging.
- Render the logo through the running Compose web app after SVG structure changes. Preview clipping in Chrome DevTools and inspect a fresh Android Chrome installation when available. State when launcher appearance remains untested.
- Check native Android drawables, desktop package resources, and iOS catalogs according to affected outputs. Static catalog consistency or dimensions on an inactive iOS target are not runtime proof. Report verified and untested targets separately.

## Selection examples

- Use: “The icon installed through Chrome looks oversized”; “Is the logo symmetric?”; “Update every platform logo from the shared SVG”; “Remove unused logo assets.”
- Do not use for: “Style the receive screen” (`atto-style`/`atto-components`); “Extract the app design system” (`design-extractor`); “Invent a new mascot image” (consider image generation).
