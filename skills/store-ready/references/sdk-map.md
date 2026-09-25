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
- **Android store URL proposal**: `https://play.google.com/store/apps/details?id=<applicationId>` — *propose*, do not assert; the app may not be listed yet. Android listings are a **list** (`storeUrlsAndroid`, one per store): if the repo shows other stores (Huawei `agconnect-services.json`, RuStore / Galaxy Store links in the README or metadata), ask for those listing URLs too — never build a non-Play URL from the application id, their paths differ per store.
- **iOS store URL**: needs the numeric App Store id — look in fastlane `Appfile`/`Deliverfile` (`apple_app_id`); otherwise ask. The `apple_id(...)` login in `Appfile` is **never** proposed as the support email.
- **AdMob publisher id**: `ca-app-pub-<16 digits>~…` in the manifest / plist gives `pub-<16 digits>`; offer `google.com, pub-<id>, DIRECT, f08c47fec0942fa0` as the first option of question 7, still to be confirmed by the developer.
- **Product flavors (Android)**: `productFlavors { … }` / `flavorDimensions` in the app module's
  `build.gradle(.kts)`. A flavor's own id is its `applicationId`, or the base `applicationId` plus
  its `applicationIdSuffix`. Its own facts live in `src/<flavor>/` (manifest, `res/values/strings.xml`
  for `app_name`) and in flavor-scoped dependency configurations — `<flavor>Implementation`,
  `<flavor>Api`, `<flavor>CompileOnly`. Flutter adds them in the same Gradle file and selects with
  `--flavor`; React Native the same, plus per-flavor `google-services.json` under `android/app/src/<flavor>/`.

  **Three non-standard layouts. Look for them, but apply one only when it is actually in the Gradle
  file.** A single-dimension build with the standard `src/<flavor>/` folders needs none of this.

  1. **Name set in Gradle, not in `strings.xml`.** Resolve each flavor's name in this order, first hit
     wins, and keep the file:line:
     `resValue("string", "app_name", "…")` in that flavor's block (Groovy: `resValue "string", "app_name", "…"`)
     → `manifestPlaceholders["appLabel"] = "…"` when the manifest says `android:label="${appLabel}"`
     (any placeholder name, follow the one the manifest uses) → a literal `android:label` in the
     flavor's own manifest → `app_name` in the flavor's `strings.xml` → the same chain in
     `defaultConfig` / `main`. A value in the flavor block beats `main` even when `main/strings.xml`
     also defines `app_name`.
  2. **Custom source sets.** A `sourceSets { … }` block (`named("pro") { res.srcDirs(…) }`, Groovy
     `pro { res.srcDirs = ['…'] }`, `manifest.srcFile(…)`) moves the flavor's folders. Read resources,
     manifest and icons from **those** paths — `srcDirs(...)` adds to the default folder, `srcDirs = [...]`
     / `setSrcDirs(...)` replaces it. A path you cannot find on disk → say so; do not fall back silently.
  3. **Several `flavorDimensions`** (e.g. `brand` × `env`). A shipped app is then a **combination**,
     one flavor per dimension (`brandAProd`), not a single flavor:
     - Its application id: an `applicationId` set in a flavor overrides the base (the flavor of the
       first-listed dimension wins); the flavors' `applicationIdSuffix` values are appended in
       `flavorDimensions` order, then the build type's. Show this derivation; if you are unsure, ask.
     - Its facts, name and icon: the combination folder `src/<brandAProd>/` first, then each flavor's
       folder in dimension order, then `main`. Dependencies: `<brandAProd>Implementation`, each
       flavor's `…Implementation`, then plain `implementation`.
     - **A dimension of environments is not a set of listings.** When a dimension's values read
       `dev`/`qa`/`test`/`staging`/`prod`/`live`, or differ only in backend URL / Firebase project, only
       the production value ships: offer `<brand> × prod` combinations only. A dimension of brands,
       tiers or white-label clients (`free`/`pro`, `clientA`/`clientB`) is one listing per value.
     - Drop combinations the build disables (`variantFilter { ignore = true }`,
       `androidComponents { beforeVariants(…) { enable = false } }`), and merge combinations whose
       final application id is the same — one id is one listing.
     - Cannot tell whether a dimension is environments or brands → show both readings in Round 0 and
       let the developer choose; never guess.
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

## SDK ids

The `sdks` field of `create_app` / `update_app` (store-form answers, `get_store_forms`). Only these
ids exist; the server refuses any other. Match on the same coordinates as the table above.

| id | Android | iOS | Cross-platform |
|---|---|---|---|
| `admob` | `play-services-ads` | `Google-Mobile-Ads-SDK` | `google_mobile_ads`, `react-native-google-mobile-ads` |
| `applovin-max` | `com.applovin:applovin-sdk` | `AppLovinSDK` | `applovin_max`, `react-native-applovin-max` |
| `unity-ads` | `com.unity3d.ads:unity-ads` | `UnityAds` | `unity_ads_plugin` |
| `firebase-analytics` | `firebase-analytics` | `FirebaseAnalytics` | `firebase_analytics`, `@react-native-firebase/analytics` |
| `mixpanel` | `com.mixpanel.android` | `Mixpanel`, `mixpanel-swift` | `mixpanel_flutter`, `mixpanel-react-native` |
| `amplitude` | `com.amplitude:analytics-android` | `AmplitudeSwift`, `Amplitude` | `amplitude_flutter`, `@amplitude/analytics-react-native` |
| `meta-app-events` | `facebook-android-sdk`, `facebook-core` | `FBSDKCoreKit` | `facebook_app_events`, `react-native-fbsdk-next` |
| `firebase-crashlytics` | `firebase-crashlytics` | `FirebaseCrashlytics` | `firebase_crashlytics`, `@react-native-firebase/crashlytics` |
| `sentry` | `io.sentry:sentry-android` | `Sentry` | `sentry_flutter`, `@sentry/react-native` |
| `firebase-auth` | `firebase-auth` | `FirebaseAuth` | `firebase_auth`, `@react-native-firebase/auth` |
| `google-sign-in` | `play-services-auth`, `googleid` (Credential Manager) | `GoogleSignIn` | `google_sign_in`, `@react-native-google-signin/google-signin` |
| `meta-login` | `facebook-login` | `FBSDKLoginKit` | `flutter_facebook_auth`, `react-native-fbsdk-next` (Login) |
| `revenuecat` | `com.revenuecat.purchases` | `RevenueCat` | `purchases_flutter`, `react-native-purchases` |
| `firebase-messaging` | `firebase-messaging` | `FirebaseMessaging` | `firebase_messaging`, `@react-native-firebase/messaging` |
| `onesignal` | `com.onesignal:OneSignal` | `OneSignalXCFramework` | `onesignal_flutter`, `react-native-onesignal` |

`react-native-fbsdk-next` covers both Meta entries: add `meta-login` only if the app calls the
login API. Nothing here is inferred from README prose.
