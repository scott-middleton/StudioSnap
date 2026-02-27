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

## Done
- [x] Add image loading/display library — using native ImageBitmap with expect/actual pattern
- [x] Error handling for restoration pipeline (API failures, credit deduction failures, retries)
- [x] Wire up recent restorations (persist and load actual restored images, not mock data)
- [x] Swap RevenueCat API keys from test to production (production ready!)
