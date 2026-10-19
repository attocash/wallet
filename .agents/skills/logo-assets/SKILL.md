---
name: "logo-assets"
version: "1.0.4"
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
- Keep `purpose: "monochrome"` entries separate from `any` and `maskable`. Generate the white silhouette on transparent bounds from the canonical geometry; alpha defines the shape and the browser chooses the displayed colors. An opaque background becomes part of that silhouette. Preserve the existing gold and opaque maskable assets. Explicit requests to prepare future support authorize adding these standard declarations even when the target launcher does not consume them yet; document that boundary and validate packaging without claiming device support.
- Check `manifest.webmanifest`, HTML favicon/Apple touch references, and the service worker's `APP_SHELL` when adding or renaming files. Every precache URL must exist in the production artifact or worker installation can fail. Inspect current hosting cache headers before overwriting stable asset URLs; prefer a new filename for a new variant.
- Keep source icon URLs plain. `composeApp/webpack.config.d/contenthash.js` owns versioning of emitted HTML, manifest, and worker URLs from actual image bytes; the artwork exporter must not write hashes into source files. Include static icon and metadata inputs in the worker's build hash and watch dependencies, so an image-only edit triggers a new cache. Modern Chrome can skip downloading installed-app icons when manifest icon entries are unchanged, even when the image or HTTP headers change. Keep the manifest URL and application identity stable. Check current [Chrome update behavior](https://developer.chrome.com/blog/improvements-to-web-app-updates) rather than promising an immediate launcher update: significant visual changes may require user acceptance.
- Kotlin's web distribution copies processed resources separately from webpack output. Preserve the distribution's exclusion of original manifest/worker copies while retaining webpack's transformed versions. Verify development responses as well as final distribution files; a source transformation or a production-only postprocessor does not establish parity.
- Preserve stable-logo revalidation in Firebase Hosting and the shared Compose SVG's precache entry. A new worker cache name does not invalidate an older immutable HTTP-cache image: the shell's reload requests prevent that stale copy from entering the new cache. Verify this boundary with a warmed old image when changing cache behavior.
- Distinguish maskable padding/cropping from Android themed-icon recoloring. Ask whether the symptom affects the home screen, app drawer, launch screen, or wallet content. Inspect the exported background before proposing a color change; a white background does not force a themed launcher to retain brand colors.
- For a themed-only contrast request, first verify that the target browser delivers a separate monochrome icon to the launcher; a valid manifest `monochrome` entry alone does not prove this. Inspect current Chromium WebAPK support when necessary. Do not recolor the shared maskable source to simulate a separate themed layer: it also changes the ordinary installed icon. Preserve existing artwork when the user wants only a themed variant.
- Before deleting an apparently unused asset, trace Android resource qualifiers and manifest resolution, desktop package icon settings, iOS `Contents.json` catalogs and project references, generated accessors, HTML, manifest, and precache references. An exact-filename text search alone is insufficient. Retained vector alternatives may be intentional; establish their role before removal.

## Verify and report

- Parse manifest/SVG syntax; confirm paths, declared dimensions, opacity, safe-circle containment, and regular/maskable separation. Review regeneration output for stale variants and unrelated changes.
- For icon URL versioning, inspect emitted files: unchanged images retain hashes, an image-only edit updates its references and worker cache version, and exact manifest URLs (including queries) are precached and available offline. Check that builds and artwork exports leave source URLs unhashed. Use a warmed immutable HTTP-cache image to verify that a new URL retrieves the updated bytes.
- Run relevant checks from `AGENTS.md`, including a production web build for packaging changes. Inspect the emitted manifest and referenced icons in the actual distribution. Kotlin compilation or tests alone do not prove resource packaging.
- Render the logo through the running Compose web app after SVG structure changes. Preview clipping in Chrome DevTools and inspect a fresh Android Chrome installation when available. State when launcher appearance remains untested.
- Check native Android drawables, desktop package resources, and iOS catalogs according to affected outputs. Static catalog consistency or dimensions on an inactive iOS target are not runtime proof. Report verified and untested targets separately.

## Selection examples

- Use: “The icon installed through Chrome looks oversized”; “Is the logo symmetric?”; “Update every platform logo from the shared SVG”; “Remove unused logo assets.”
- Do not use for: “Style the receive screen” (`atto-style`/`atto-components`); “Extract the app design system” (`design-extractor`); “Invent a new mascot image” (consider image generation).
