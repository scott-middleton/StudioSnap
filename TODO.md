# TODO

## In Progress

## Up Next

## Done
- [x] Resize-to-upload now matches Flux Kontext's actual resolution buckets instead of a flat 1024x1024 box — the model always snaps conditioning images to the closest of 17 fixed ~1MP resolutions by aspect ratio regardless of upload size (traced through black-forest-labs/flux's `prepare_kontext`), so a flat square cap under-shot non-square photos (e.g. a 3:4 portrait only got 768x1024/0.79MP when the model wanted ~880x1184/1.03MP). `closestKontextResolution()` (ImageResizer.kt) replicates the model's own bucket-selection formula; `GenerationRepositoryImpl` reads the source image's real dimensions and resizes to the matching bucket, falling back to the old flat cap only if dimension-reading fails. Covered by ImageResizerTest. Model stays Flux Kontext dev (tested against alternatives, performs better) — no model swap.
- [x] Image compression before upload to API — already implemented (resize to closest Kontext bucket + JPEG q85 before base64 upload)
- [x] Result screen UI improvements — reviewed: current Before/After toggle is preferred over a swipe slider; restored image already shows first by default; fullscreen pinch-to-zoom (FullScreenImageOverlay) and rotation/landscape lock already wired in
- [x] Generation error UX (decision: option b) — when every unit in a batch fails, stay on the Processing screen with an AllFailed error state (Retry / Go Back, refund count in message) instead of navigating to the dead-end Results screen. Any success still navigates to Results as before. Retry after all-failed resets the batch resume state so all units re-run from scratch.
- [x] Onboarding/examples screen example images updated (done outside this workflow)
- [x] Feature: one photo × multiple styles (constrained modes: multi-select up to 4 styles only when exactly 1 photo; cost = photos × styles; "N styles" history label; per-item style labels in results/session detail when mixed). Pipeline generalized to (photo, style) units. Includes prerequisite fix: batchId now persisted, so generation runs group into one history session (legacy rows unaffected).
- [x] History detail gallery button — root cause: images WERE saved to the gallery (Results auto-save), but the returned gallery URI was discarded; the button then passed the app-private previewUri to ACTION_VIEW, which other apps can't read → dead button / broken image. Fix (pattern from ImageCloneAI): persist galleryUri on the generation row (DB v5 auto-migration), SaveToGalleryUseCase now records it, session detail opens the real gallery URI and self-heals legacy rows by saving first. iOS unchanged (opens Photos app; localIdentifier now persisted for future use).
- [x] History detail — tapping a generated photo now opens the existing FullScreenImageOverlay (pinch-to-zoom, landscape lock, back-press dismiss), same wiring idiom as ResultsScreen
- [x] History showed raw style id instead of friendly name — root cause: styleName column stores style.id for localized (StringResource) styles since there's no string context at save time. Fix: resolve the localized displayName from styleId via StyleRepository at display time (history list + session detail title); falls back to stored raw name only if the style was removed. Retroactively fixes existing rows.
- [x] Onboarding text unreadable in dark mode — audited all onboarding text vs. backgrounds; text on hardcoded-white cards now uses static AppColors.Ink*, text on theme-aware backgrounds uses extendedColorScheme().ink*
- [x] Processing screen literal quote marks around apostrophe — unescaped `\'` to raw `'` in strings.xml (workaround for Compose Multiplatform 1.8.x escaping bug)
- [x] Add image loading/display library — using native ImageBitmap with expect/actual pattern
- [x] Error handling for restoration pipeline (API failures, credit deduction failures, retries)
- [x] Wire up recent restorations (persist and load actual restored images, not mock data)
- [x] Swap RevenueCat API keys from test to production (production ready!)
