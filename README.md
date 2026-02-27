This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you’re sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

1. SplashScreen
   Duration: 1-2 seconds
   Credits Display: No
   Content:

App logo (centered)
App name
Subtle loading animation
Background gradient or brand color


2. OnboardingCarousel
   Screens: 3 swipeable screens
   Credits Display: No
   Screen 2.1 - Hero:

Headline: "Restore Your Precious Memories"
Subheadline: "AI-powered photo restoration in seconds"
Hero image/animation showing transformation
"Next" button

Screen 2.2 - Interactive Demo:

Headline: "See The Magic"
2-3 before/after sliders (different photo types)

Old damaged photo
Black & white photo
Faded color photo


"Next" button

Screen 2.3 - Value Proposition:

Headline: "Simple & Fair Pricing"
Benefits list:

✓ No subscriptions
✓ Pay only for what you use
✓ Professional quality results
✓ Ready in 5 seconds


"Get Started" CTA button → MainRestoreScreen


3. MainRestoreScreen
   Credits Display: Yes (in app bar)
   App Bar:

App name/logo (left)
Credit chip (right):

If has credits: "🪙 12"
If no credits: "Get Started - 3 for £0.99"
Tappable → TokenPurchaseSheet



Content:

Large image placeholder with dashed border
Icon: Camera/Gallery icon
Primary CTA: "Select Photo from Gallery"
Secondary text: "Choose a photo to restore"
Bottom info: "1 token per restoration • +1 for color"

Photo Selection Bottom Sheet (after photo selected):

Selected image preview
Options:

□ Restore damage (1 token)
□ Add color (2 tokens total)


Total cost: "This will use X tokens"
"Restore Now" button (disabled if insufficient tokens)
"Cancel" option


4. ProcessingScreen
   Duration: ~5 seconds
   Credits Display: Yes (in app bar)
   Content:

Selected photo (blurred/faded) as background
Centered progress animation with sparkles
Progress stages text (cycles through):

"Analyzing image quality..."
"Detecting damage patterns..."
"Reconstructing missing details..."
"Applying AI enhancement..."
"Finalizing your photo..."


Circular progress indicator
No cancel option


5. ResultScreen
   Credits Display: Yes (in app bar)
   Content:

Before/After slider (full width, prominent)

Smooth drag interaction
"Before" / "After" labels


Action buttons:

Primary: "Share Your Transformation" → Share sheet
Secondary: "Download" (auto-saves to gallery)
Tertiary: "Restore Another Photo"


Success animation on first view (sparkles/celebration)

Share Functionality:

Creates side-by-side comparison image
Small watermark: "Restored with [AppName]"
Native share sheet

Review Prompt (conditional):

Appears after 3rd restoration OR after sharing
Native in-app review dialog


6. TokenPurchaseSheet (Bottom Sheet)
   Credits Display: Yes (shows current balance at top)
   When triggered from app bar:

Header: "Your Tokens: X"
Subheader: "Add more tokens to continue restoring"

When triggered by insufficient tokens:

Background: Selected photo (blurred)
Header: "Your photo is ready to restore!"
Subheader: "You need X more tokens"
First-time bonus banner: "🎉 Limited time: 20% bonus tokens on first purchase"

Token Packs (all scenarios):

3 tokens - £0.99 "Try it out"
10 tokens - £2.99 "Small pack"
50 tokens - £11.99 "BEST VALUE" (pre-selected)
100 tokens - £19.99 "Power user"

Bottom Section:

Purchase button: "Continue with Apple Pay" / "Continue with Google Pay"
Trust text: "Secure payment • Cancel anytime"
Token addition animation after successful purchase


7. AuthBottomSheet (Post-Purchase Overlay)
   Triggers: After successful payment (if not logged in)
   Content:

Header: "Secure Your Tokens"
Subheader: "Save your tokens and restoration history"
Social login buttons:

"Continue with Google"
"Continue with Apple"


Email option: "Use Email Instead"
Skip option: "Maybe Later" (small text)
Benefits note: "Access your tokens on any device"


8. MenuSheet (Bottom Sheet)
   Access: Profile icon in app bar (when logged in)
   Credits Display: Yes (in parent screen)
   Content:

User email/name
Menu items:

Restoration History
Buy More Tokens → TokenPurchaseSheet
Help & FAQ
Rate Us → App Store
Sign Out


App version (bottom)


Persistent Elements
Credit Chip Design:
[🪙 12]  - Has tokens (tappable)
[Get Started - £0.99] - No tokens (pulsing animation)
Token Addition Animation:

Numbers roll up from old to new count
Sparkle burst effect
Subtle haptic feedback

Navigation:

Back navigation available except during processing
Bottom sheets dismissible with swipe down
Maintain state when returning to previous screens

This specification provides a complete, user-tested flow optimized for conversion while maintaining a clean, professional experience. Each screen has a clear purpose and moves users toward either trying the app or making a purchase.

Analytics & Tracking Plan
Core KPIs to Track
1. Conversion Metrics

Trial → First Purchase Rate: % who buy after seeing the app capabilities
Visitor → Paying User: Overall conversion rate
Time to First Purchase: Average minutes from app open to payment
Cart Abandonment Rate: % who open purchase sheet but don't buy

2. Revenue Metrics

ARPU (Average Revenue Per User)
ARPPU (Average Revenue Per Paying User)
Token Pack Distribution: Which packs sell most
Repeat Purchase Rate: % who buy again within 7/30 days
LTV (Lifetime Value) by acquisition source

3. Engagement Metrics

DAU/MAU: Daily/Monthly Active Users
Restorations per User: Average count
Share Rate: % of restorations shared
Feature Adoption: % using colorization vs just restoration
Session Duration: Time spent in app