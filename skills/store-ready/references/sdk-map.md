# Dependency / permission → data-collection fact

Facts feed `dataCollection` (`account`, `analytics`, `ads`, `crash`, `location`, `purchases`, `none`).
Match on dependency coordinates, pod names, SwiftPM products and manifest/plist keys. Report the
**file and line** each fact came from. When nothing matches, the fact list is `["none"]` — say so.

| Fact | Android (`build.gradle(.kts)`, `libs.versions.toml`, manifest) | iOS (`Podfile`, `Package.swift`, `*.pbxproj`, `Info.plist`) | Cross-platform (`pubspec.yaml`, `package.json`, `app.json`) |
|---|---|---|---|
| `ads` | `play-services-ads`, `com.google.android.gms:play-services-ads*`, `applovin`, `unity-ads`, `ironsource`, `inmobi`, `vungle`, `chartboost`, `AD_ID` permission | `Google-Mobile-Ads-SDK`, `AppLovinSDK`, `UnityAds`, `IronSourceSDK`, `NSUserTrackingUsageDescription`, `SKAdNetworkItems` | `google_mobile_ads`, `react-native-google-mobile-ads`, `expo-ads-admob`, `applovin_max` |
| `analytics` | `firebase-analytics`, `com.google.firebase:firebase-analytics*`, `mixpanel`, `amplitude`, `segment`, `com.facebook.android:facebook-android-sdk` (App Events), `appsflyer`, `adjust` | `FirebaseAnalytics`, `Mixpanel`, `Amplitude`, `Segment`, `FBSDKCoreKit`, `AppsFlyerFramework`, `Adjust` | `firebase_analytics`, `@react-native-firebase/analytics`, `mixpanel_flutter`, `amplitude_flutter`, `expo-firebase-analytics` |
| `crash` | `firebase-crashlytics`, `io.sentry:sentry-android`, `bugsnag-android`, `com.instabug` | `FirebaseCrashlytics`, `Sentry`, `Bugsnag`, `Instabug` | `firebase_crashlytics`, `sentry_flutter`, `@sentry/react-native`, `bugsnag` |
| `location` | `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`, `ACCESS_BACKGROUND_LOCATION`, `play-services-location`, `play-services-maps` | `NSLocationWhenInUseUsageDescription`, `NSLocationAlwaysAndWhenInUseUsageDescription`, `CoreLocation`, `GoogleMaps` pod | `geolocator`, `location`, `google_maps_flutter`, `expo-location`, `react-native-geolocation-service` |
| `purchases` | `com.android.billingclient:billing`, `revenuecat`/`purchases`, `qonversion`, `adapty` | `StoreKit` usage, `RevenueCat`/`Purchases` pod, `Qonversion`, `Adapty`, `SKPaymentQueue` in source *names* only | `in_app_purchase`, `purchases_flutter`, `react-native-iap`, `react-native-purchases`, `expo-in-app-purchases` |
| `account` | `firebase-auth`, `play-services-auth`, `credentials`, `auth0`, `supabase` auth, `amplify-auth`, `com.facebook.android:facebook-login` | `FirebaseAuth`, `GoogleSignIn`, `AuthenticationServices` (Sign in with Apple), `Auth0`, `FBSDKLoginKit` | `firebase_auth`, `google_sign_in`, `sign_in_with_apple`, `@react-native-firebase/auth`, `expo-auth-session`, `supabase_flutter` |

Also derive (not a `dataCollection` key, but used in the pages and the questions):

- **Platform**: Android files present → `android`; iOS files present → `ios`; both → `both`.
- **App name**: `res/values/strings.xml` `app_name`; `CFBundleDisplayName` / `CFBundleName`; `pubspec.yaml` `name`; `app.json` `expo.name`.
- **Application id / bundle id**: `applicationId` in the app module's Gradle file; `PRODUCT_BUNDLE_IDENTIFIER` in `project.pbxproj`; `android.package` / `ios.bundleIdentifier` in `app.json`.
- **Android store URL proposal**: `https://play.google.com/store/apps/details?id=<applicationId>` — *propose*, do not assert; the app may not be listed yet.
- **iOS store URL**: needs the numeric App Store id — look in fastlane `Appfile`/`Deliverfile`; otherwise ask.
- **Children / age gate**: `com.google.android.gms.ads.flag` / `tagForChildDirectedTreatment`, `setMaxAdContentRating`, `NSChildrenApp` hints → mention in the questions; **do not** write COPPA claims into the policy without the developer confirming.
