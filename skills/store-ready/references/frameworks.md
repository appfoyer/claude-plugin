# Where the identity lives — by framework

Step 1 of the skill. Identify the framework first, then read **only** the files named for it. The
native rules in `sdk-map.md` (flavors, targets, non-standard layouts) still apply inside the native
folders; this file says which folders those are and what the framework puts in front of them.

| Marker (repo root or one level down) | Framework |
|---|---|
| `pubspec.yaml` with a `flutter:` section, plus `android/` and/or `ios/` | Flutter |
| `package.json` depending on `expo` | Expo (React Native) |
| `package.json` depending on `react-native`, no `expo` | React Native (bare) |
| A Gradle module with a `kotlin { … }` block declaring `androidTarget()` and/or `iosArm64()` / `iosSimulatorArm64()` / `iosX64()` | Kotlin Multiplatform |
| None of the above | Native — `sdk-map.md` only |

A monorepo can hold several of these (a Flutter app next to a web app, two RN apps under `apps/`).
List every mobile app you find with its folder and ask which one is meant, unless the developer
started Claude in that app's folder or named its package id.

## Rules for every cross-platform project

- **The Android app module is the one that applies `com.android.application`** (`id("com.android.application")`,
  `alias(libs.plugins.android.application)`, Groovy `apply plugin: 'com.android.application'`). Never
  assume it is called `app`. Its `applicationId` is the package id; **`namespace` is not** — it is the
  Kotlin/R package and often differs from the id the store knows.
- **The iOS app is the target whose product type is `com.apple.product-type.application`** in
  `project.pbxproj`. Test targets (`RunnerTests`, `<Name>Tests`, `…UITests`) and app extensions are
  not listings. Read its `PRODUCT_BUNDLE_IDENTIFIER` from the **Release** build configuration of that
  target (Flutter also has `Profile`; both normally match Release).
- **A bundle id written as a variable** (`$(PRODUCT_BUNDLE_IDENTIFIER)`, `${BUNDLE_ID}${TEAM_ID}`,
  `$(APP_ID)`) is resolved from the `.xcconfig` the build configuration names
  (`baseConfigurationReference`) and the files it `#include`s. Show the chain with file:line. A value
  that only a CI variable or a secret supplies cannot be resolved — say so and ask.
- **Platform** = which native app exists: an Android app module → `android`, an iOS app target →
  `ios`, both → `both`. A folder the framework generated but the developer never ships is common
  (Flutter creates `ios/` on every `flutter create`): print the platform as a derived fact with its
  source, so the developer can correct it in Round 1 rather than being asked.
- **Dependencies come from the framework's manifest, not from the native build files.** Flutter and
  React Native plugins are autolinked: they do not appear in `android/app/build.gradle` or the
  `Podfile`, and their permissions are added by manifest merge, which the repository does not
  contain. Map `pubspec.yaml` / `package.json` entries through `sdk-map.md`; the native files add
  whatever is declared there **by hand** (a pod, a Gradle line, a permission, a plist key).
  `Podfile.lock` / `pubspec.lock` / lockfiles may confirm a transitive pod but never create a fact
  on their own. Skip `dev_dependencies` / `devDependencies` — they do not ship. **But say so** when a
  data SDK sits there (`sentry_flutter`, `@react-native-firebase/crashlytics` under dev): one line
  asking whether it ships, because a misplaced crash SDK is an undisclosed one. Its answer decides.
- **Resolving variables** — the same for bundle ids and names (`$(PRODUCT_NAME)` → `PRODUCT_NAME` in
  the pbxproj → `APP_NAME` in the `.xcconfig`; `${appName}` → `manifestPlaceholders`): follow the
  chain and cite each step. A variable **defined empty** (`TEAM_ID=`) is the empty string, not a gap.
  An `#include` of a file that is not in the repository (Flutter's `Generated.xcconfig`, always
  gitignored) is ignored when the value is found elsewhere; if the value can only come from it, ask.
- **Which configuration belongs to the app target**: target → `buildConfigurationList` →
  `XCConfigurationList.buildConfigurations` → the one named `Release`. If that link is missing, match
  on `INFOPLIST_FILE` pointing into the app folder and say you did.
- **Check the ids before proposing anything.** An id beginning `com.example.` is Flutter's and
  Android Studio's default and cannot be uploaded to Google Play — say so and ask for the real one; do
  not build a store URL from it. Android and iOS ids that differ **only in letter case**
  (`habitloop` / `habitLoop`) are legal but often a slip: point it out, then use each as written. An
  AdMob app id of zeros or Google's sample publisher `ca-app-pub-3940256099942544` is a test id:
  offer no `app-ads.txt` line from it.
- **Citing sources**: a `libs.…` alias cites the Gradle usage line **and** the `libs.versions.toml`
  coordinate line; a multi-line entry cites its range (`AndroidManifest.xml:6-8`); a fact found in two
  places (the package and its Expo config plugin) cites both.
- **What to name as "not covered by the store-form answers"**: SDKs that collect or transmit data
  (`sign_in_with_apple`, `expo-location`). Framework, core and UI packages (`flutter`, `firebase_core`,
  `@react-native-firebase/app`, `expo`, `expo-router`, `react`, `react-native`) are not named.
- **Permissions for the privacy page** are the ones the app asks the user for: Android *dangerous*
  permissions and `AD_ID`, iOS `NS…UsageDescription` keys. `INTERNET`, `ACCESS_NETWORK_STATE`,
  `WAKE_LOCK` and other install-time permissions are not listed. A permission a plugin adds at build
  time is not in the repo and is not a fact of its own — the SDK behind it already is.
- **A fact on one platform only** (an `androidMain` SDK, an Android-only permission, a pod the
  Android build lacks) still goes into the one `dataCollection` list of a `both` app, and the privacy
  draft says which platform it applies to ("On Android, the app uses your location to …").

## Flutter

| What | Where, first hit wins |
|---|---|
| Android package id | `android/app/build.gradle(.kts)` → `applicationId` (in `defaultConfig`, then each flavor) |
| iOS bundle id | `ios/Runner.xcodeproj/project.pbxproj` → the `Runner` target's `PRODUCT_BUNDLE_IDENTIFIER` (Release); variables via `ios/Flutter/*.xcconfig` |
| App name (what the store shows) | `android/app/src/main/AndroidManifest.xml` `android:label` (follow `@string/…` into `res/values/strings.xml`) · `ios/Runner/Info.plist` `CFBundleDisplayName`, then `CFBundleName` |
| Dependencies | `pubspec.yaml` `dependencies:` |
| Permissions | `android/app/src/main/AndroidManifest.xml`, `ios/Runner/Info.plist` (`NS…UsageDescription`) |
| AdMob app id | manifest meta-data `com.google.android.gms.ads.APPLICATION_ID`, `Info.plist` `GADApplicationIdentifier` |
| Icon | `flutter_launcher_icons.yaml` / `pubspec.yaml` `flutter_launcher_icons:` → `image_path` (and `image_path_android` / `image_path_ios`), else the native paths in SKILL.md 5b under `android/app/src/main/res` and `ios/Runner/Assets.xcassets` |

- **`pubspec.yaml` `name` is the Dart package name** (`trail_notes`), not the app's name. Use it only
  when neither native file has a label, and then turn it into words (`Trail Notes`) and say so.
- `android:label` / `CFBundleDisplayName` may be a variable (`${appName}`, `$(APP_DISPLAY_NAME)`):
  resolve it through `manifestPlaceholders` / `resValue` in Gradle and the `.xcconfig`, as in
  `sdk-map.md` → "Three non-standard layouts".
- **Flavors.** Android: `productFlavors` in `android/app/build.gradle(.kts)`, exactly as native.
  iOS: one build configuration per flavor (`Release-free`, `Release-pro`), a scheme per flavor in
  `ios/Runner.xcodeproj/xcshareddata/xcschemes/`, and the bundle id set per configuration or in
  `ios/Flutter/<flavor>.xcconfig`. Pair an Android flavor with the iOS configuration of the **same
  flavor name**; an unpaired one is a single-platform app. `flutter_flavorizr` projects state the
  whole table in `flavorizr.yaml` (or a `flavorizr:` key in `pubspec.yaml`) — `flavors.<name>.android.applicationId`
  and `flavors.<name>.ios.bundleId` — and that file is the best source when present; still check
  the Gradle file agrees and report a disagreement instead of picking one.
- A per-flavor `lib/main_<flavor>.dart` only tells you the flavor exists; do not open it.

## React Native (bare)

| What | Where |
|---|---|
| Android package id | `android/app/build.gradle` → `applicationId` |
| iOS bundle id | `ios/<Name>.xcodeproj/project.pbxproj` → the application target's `PRODUCT_BUNDLE_IDENTIFIER` (Release) |
| App name | manifest `android:label` → `res/values/strings.xml` `app_name` · `ios/<Name>/Info.plist` `CFBundleDisplayName`; `app.json` `displayName` as fallback |
| Dependencies | `package.json` `dependencies` |
| Permissions | `android/app/src/main/AndroidManifest.xml`, `ios/<Name>/Info.plist` |

- `app.json` `name` is the JS module name registered with `AppRegistry` (`TrailNotes`), not the
  store name.
- Flavors: `productFlavors` in `android/app/build.gradle` and iOS schemes/targets, as native. Many
  RN projects switch variants with `react-native-config` and `.env.<variant>` files: the **file
  names** tell you which variants exist; **never open them** (the `.env*` rule stands). A bundle id
  that only such a file sets cannot be derived — ask.

## Expo

| What | Where, first hit wins |
|---|---|
| Android package id | `expo.android.package` |
| iOS bundle id | `expo.ios.bundleIdentifier` |
| App name | `expo.name` |
| Platforms | `expo.platforms` if set; else a platform whose id is present |
| Dependencies | `package.json` `dependencies` **and** `expo.plugins` (config plugins, e.g. `react-native-google-mobile-ads` with `androidAppId` / `iosAppId`) |
| Permissions | `expo.android.permissions`, `expo.ios.infoPlist` usage keys, and plugin options that set them (`expo-location`'s `locationWhenInUsePermission`) |
| Icon | `expo.icon`, `expo.android.adaptiveIcon.foregroundImage` is not a store icon (it is a layer) |

`expo.slug` is Expo's project slug, not ours; never propose it as an AppFoyer address without the
developer's yes.

- **Which file is the config.** `app.json`, or `app.config.js` / `app.config.ts`. A JS/TS config is
  code: read its literal values only, never run it. A value computed from `process.env.X` (the usual
  `APP_VARIANT` pattern) is a **variant**: list each branch you can read (`IS_DEV ? 'com.acme.dev' :
  'com.acme'`) with its condition, treat the dev/preview branches as build types, and ask if a value
  comes from an env var you cannot see. A profile that does not set the variable takes the branch
  for "unset" — `eas.json` answers that, no question needed.
- **Variants.** `eas.json` → `build.<profile>.env.APP_VARIANT` (or similar) names the profiles that
  set it. `development` / `preview` profiles are not listings; a profile per brand is one listing each.
- **Permissions** a config plugin sets for Android (`expo-location` adds the location permissions)
  are not in the repo: cite the plugin line in `expo.plugins` as their source.
- `@react-native-firebase/*` in an Expo app needs `@react-native-firebase/app` in `expo.plugins` and a
  `googleServicesFile`. Without them the dependency is still a fact; mention that it looks unconfigured.
- **Native folders.** If `android/` and `ios/` are committed (not in `.gitignore`), EAS builds from
  them and ignores the native keys in the config — read the ids **from the native files** as for bare
  React Native, and report any disagreement with `app.json`. If they are ignored or absent
  (Continuous Native Generation), the config is the only truth.

## Kotlin Multiplatform

| What | Where |
|---|---|
| Android package id | the module applying `com.android.application` — `composeApp/`, `androidApp/`, or `app/` — `applicationId` in its `android { defaultConfig { … } }` |
| iOS bundle id | `iosApp/iosApp.xcodeproj/project.pbxproj` (the folder name varies; find the `.xcodeproj` with an application target). The wizard template sets it through `iosApp/Configuration/Config.xcconfig` (`BUNDLE_ID=…`, `PRODUCT_BUNDLE_IDENTIFIER` built from it) — resolve the variables |
| App name | the Android module's manifest `android:label` → `strings.xml`; `iosApp/iosApp/Info.plist` `CFBundleDisplayName`, or `APP_NAME` in `Config.xcconfig` |
| Dependencies | every module's `build.gradle.kts` that the app module depends on (`implementation(projects.shared)`), plus `gradle/libs.versions.toml` for the coordinates behind `libs.…` aliases |
| Permissions | the Android module's `AndroidManifest.xml` — `src/main/` in a plain Android module, `src/androidMain/` when the app module is itself a KMP module (and the shared module's `src/androidMain/AndroidManifest.xml`, merged into it), the iOS app's `Info.plist` |

- **Source sets decide the platform of a fact.** `commonMain.dependencies { … }` is a fact for both
  platforms; `androidMain` only for Android; `iosMain` (and `iosArm64Main` …) only for iOS. Keep the
  platform with the file:line — an Android-only ad SDK never goes on the iOS policy of the same app.
- **iOS dependencies can come from three places:** the `cocoapods { pod("FirebaseAuth") }` block in
  the shared module's Gradle file, a `Podfile` in the iOS folder, and Swift packages in the Xcode
  project (`XCRemoteSwiftPackageReference` in `project.pbxproj`). Read all three.
- `iosX64` / `iosSimulatorArm64` are simulator targets, not listings; one iOS app target is one app.
- Other targets (`jvm("desktop")`, `wasmJs`, `js`) are not store listings; ignore them.
- Flavors: the Android app module's `productFlavors`, as native; iOS targets/schemes, as native.
- A KMP wrapper in `commonMain` (`dev.gitlive:firebase-*`, `sentry-kotlin-multiplatform`) needs the
  native SDK linked on iOS too (a pod, an SPM package). If none of the three iOS sources has it, keep
  the fact for both platforms but tell the developer the iOS side looks unlinked.
- KMP libraries map to facts like their native counterparts — `sdk-map.md` names the common ones
  (`dev.gitlive:firebase-*`, `sentry-kotlin-multiplatform`, `purchases-kmp`, `supabase` modules).
