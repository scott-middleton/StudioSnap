# TODO

## In Progress

## Up Next
- [ ] Image compression before upload to API
- [ ] Investigate image size limitations and upscaling behavior — if a high-res image is downsampled due to API limits, the final upscale may not match the user's requested 2x/3x. Need to determine: should we warn the user? Skip downsampling and fail? Show expected output size before processing?
- [ ] Result screen UI improvements:
  - [ ] Use the same swipe comparison view as onboarding (instead of current side-by-side or tabs)
  - [ ] Default to showing the restored image first
  - [ ] Add fullscreen enlargement with pinch-to-zoom
  - [ ] Support rotation like the onboarding image comparisons

## Done
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
