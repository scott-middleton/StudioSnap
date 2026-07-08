# TODO

## In Progress

## Up Next
- [ ] Image compression before upload to API
- [ ] Update example images in onboarding and examples screen (use images from main restore screen)
- [ ] Investigate image size limitations and upscaling behavior — if a high-res image is downsampled due to API limits, the final upscale may not match the user's requested 2x/3x. Need to determine: should we warn the user? Skip downsampling and fail? Show expected output size before processing?
- [ ] Result screen UI improvements:
  - [ ] Use the same swipe comparison view as onboarding (instead of current side-by-side or tabs)
  - [ ] Default to showing the restored image first
  - [ ] Add fullscreen enlargement with pinch-to-zoom
  - [ ] Support rotation like the onboarding image comparisons
- [ ] History list item shows the raw style name (looks like snake_case/Pascal_Case) instead of a human-readable label — find where the string comes from and map it to a friendly display name
- [ ] History detail — tapping a generated photo doesn't enlarge it full-screen like elsewhere in the app; reuse the existing enlarge component
- [ ] History detail — gallery toolbar button doesn't open the gallery, or opens to a broken/placeholder image; suspect generated images aren't actually being saved to the device gallery — needs investigation
- [ ] Generation error UX (NEEDS DECISION): a failed single-image generation still navigates to the Results screen, which shows a centered failure message but no action buttons and no clear next step. Decide: (a) keep Results screen but add a proper error state with contextual message + retry/exit buttons, or (b) don't navigate to Results screen on failure at all and surface the error earlier while keeping the user on the previous screen
- [ ] Feature: allow one product image → multiple styles, not just multiple images → one style. Needs UX design so both usage patterns (many-images/one-style vs. one-image/many-styles) coexist without confusing the picker/generation flow

## Done
- [x] Onboarding text unreadable in dark mode — audited all onboarding text vs. backgrounds; text on hardcoded-white cards now uses static AppColors.Ink*, text on theme-aware backgrounds uses extendedColorScheme().ink*
- [x] Processing screen literal quote marks around apostrophe — unescaped `\'` to raw `'` in strings.xml (workaround for Compose Multiplatform 1.8.x escaping bug)
- [x] Add image loading/display library — using native ImageBitmap with expect/actual pattern
- [x] Error handling for restoration pipeline (API failures, credit deduction failures, retries)
- [x] Wire up recent restorations (persist and load actual restored images, not mock data)
- [x] Swap RevenueCat API keys from test to production (production ready!)
