# Dependency / permission → data-collection fact

Facts feed `dataCollection` (`account`, `analytics`, `ads`, `crash`, `location`, `purchases`, `none`).
Match on dependency coordinates, pod names, SwiftPM products and manifest/plist keys. Report the
**file and line** each fact came from. When nothing matches, the fact list is `["none"]` — say so.

| Fact | Android (`build.gradle(.kts)`, `libs.versions.toml`, manifest) | iOS (`Podfile`, `Package.swift`, `*.pbxproj`, `Info.plist`) | Cross-platform (`pubspec.yaml`, `package.json`, `app.json`) |
|---|---|---|---|
| `ads` | `play-services-ads`, `com.google.android.gms:play-services-ads*`, `com.google.android.gms.ads.APPLICATION_ID` meta-data, `applovin`, `unity-ads`, `ironsource`, `inmobi`, `vungle`, `chartboost`, `AD_ID` permission | `Google-Mobile-Ads-SDK`, `AppLovinSDK`, `UnityAds`, `IronSourceSDK`, `NSUserTrackingUsageDescription`, `SKAdNetworkItems` | `google_mobile_ads`, `react-native-google-mobile-ads`, `expo-ads-admob`, `applovin_max` |
| `analytics` | `firebase-analytics`, `com.google.firebase:firebase-analytics*`, `mixpanel`, `amplitude`, `segment`, `com.facebook.android:facebook-android-sdk` (App Events), `appsflyer`, `adjust` | `FirebaseAnalytics`, `Mixpanel`, `Amplitude`, `Segment`, `FBSDKCoreKit`, `AppsFlyerFramework`, `Adjust` | `firebase_analytics`, `@react-native-firebase/analytics`, `mixpanel_flutter`, `amplitude_flutter`, `expo-firebase-analytics` |
| `crash` | `firebase-crashlytics`, `io.sentry:sentry-android`, `bugsnag-android`, `com.instabug` | `FirebaseCrashlytics`, `Sentry`, `Bugsnag`, `Instabug` | `firebase_crashlytics`, `sentry_flutter`, `@sentry/react-native`, `bugsnag` |
| `location` | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `play-services-location`, `play-services-maps` | `NSLocationWhenInUseUsageDescription`, `NSLocationAlwaysAndWhenInUseUsageDescription`, `CoreLocation`, `GoogleMaps` pod | `geolocator`, `location`, `google_maps_flutter`, `expo-location`, `react-native-geolocation-service` |
| `purchases` | `com.android.billingclient:billing`, `revenuecat`/`purchases`, `qonversion`, `adapty` | `StoreKit` usage, `RevenueCat`/`Purchases` pod, `Qonversion`, `Adapty`, `SKPaymentQueue` in source *names* only | `in_app_purchase`, `purchases_flutter`, `react-native-iap`, `react-native-purchases`, `expo-in-app-purchases` |
| `account` | `firebase-auth`, `play-services-auth`, `credentials`, `auth0`, `supabase` auth, `amplify-auth`, `com.facebook.android:facebook-login` | `FirebaseAuth`, `GoogleSignIn`, `AuthenticationServices` / `com.apple.developer.applesignin` entitlement (Sign in with Apple), `Auth0`, `FBSDKLoginKit` | `firebase_auth`, `google_sign_in`, `sign_in_with_apple`, `@react-native-firebase/auth`, `expo-auth-session`, `supabase_flutter` |

Also derive (not a `dataCollection` key, but used in the pages and the questions):

- **Platform**: Android files present → `android`; iOS files present → `ios`; both → `both`.
- **App name**: `res/values/strings.xml` `app_name`; `CFBundleDisplayName` / `CFBundleName`; `pubspec.yaml` `name`; `app.json` `expo.name`.
- **Application id / bundle id**: `applicationId` in the app module's Gradle file; `PRODUCT_BUNDLE_IDENTIFIER` in `project.pbxproj`; `android.package` / `ios.bundleIdentifier` in `app.json`.
- **Android store URL proposal**: `https://play.google.com/store/apps/details?id=<applicationId>` — *propose*, do not assert; the app may not be listed yet.
- **iOS store URL**: needs the numeric App Store id — look in fastlane `Appfile`/`Deliverfile` (`apple_app_id`); otherwise ask. The `apple_id(...)` login in `Appfile` is **never** proposed as the support email.
- **AdMob publisher id**: `ca-app-pub-<16 digits>~…` in the manifest / plist gives `pub-<16 digits>`; offer `google.com, pub-<id>, DIRECT, f08c47fec0942fa0` as the first option of question 7, still to be confirmed by the developer.
- **Product flavors (Android)**: `productFlavors { … }` / `flavorDimensions` in the app module's
  `build.gradle(.kts)`. A flavor's own id is its `applicationId`, or the base `applicationId` plus
  its `applicationIdSuffix`. Its own facts live in `src/<flavor>/` (manifest, `res/values/strings.xml`
  for `app_name`) and in flavor-scoped dependency configurations — `<flavor>Implementation`,
  `<flavor>Api`, `<flavor>CompileOnly`. Flutter adds them in the same Gradle file and selects with
  `--flavor`; React Native the same, plus per-flavor `google-services.json` under `android/app/src/<flavor>/`.
- **Targets / schemes (iOS)**: a second target with its own `PRODUCT_BUNDLE_IDENTIFIER` in
  `project.pbxproj`, usually with its own `Info.plist`, `.entitlements` and `.xcconfig`. Shared
  schemes live in `*.xcodeproj/xcshareddata/xcschemes/`. App extensions (widget, share, notification
  service — bundle ids *under* the app's own, `com.acme.app.widget`) are **not** flavors: they ship
  inside one listing.
- **Not a flavor**: `buildTypes` (`debug`, `release`), a `.debug` / `.staging` `applicationIdSuffix`,
  a `-dev` scheme. They never reach a store listing, so they never get an app. When in doubt, the
  test is whether it has its own store listing — ask, do not assume.
- **Per-flavor store URL**: build each flavor's proposal from **its own** application id. Two
  flavors sharing one Play URL is always a mistake in the derivation.
- **Children / age gate**: `com.google.android.gms.ads.flag` / `tagForChildDirectedTreatment`, `setMaxAdContentRating`, `NSChildrenApp` hints → mention in the questions; **do not** write COPPA claims into the policy without the developer confirming.
