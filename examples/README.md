# Sample repos for the store-ready dry run

Minimal repositories that contain **only the files the skill is allowed to read** (build,
manifest, plist, store metadata). No source code, so the skill's "derive, don't ask" rule can be
checked in isolation.

| Repo | Expected derived facts | Expected identity |
|---|---|---|
| `android-sample/` | `account` (firebase-auth), `analytics` (firebase-analytics), `crash` (firebase-crashlytics), `ads` (play-services-ads + `AD_ID`), `location` (`ACCESS_FINE_LOCATION`) | name `Trail Notes`, id `com.example.trailnotes`, platform `android`, Play URL proposed from the id |
| `ios-sample/` | `account` (FirebaseAuth + AuthenticationServices), `purchases` (RevenueCat), `crash` (Sentry), `location` (`NSLocationWhenInUseUsageDescription`) | name `Pocket Ledger`, bundle `com.example.pocketledger`, platform `ios`, App Store id `id1234567890` from `fastlane/Appfile` |
| `flutter-sample/` | `account` (firebase_auth, sign_in_with_apple), `ads` (google_mobile_ads + AdMob app id + `NSUserTrackingUsageDescription`), `purchases` (purchases_flutter). **Not** `crash`: `sentry_flutter` is a dev dependency | name `Habit Loop` (manifest `android:label` / `CFBundleDisplayName`, not pubspec `habit_loop`), Android `com.example.habitloop` (`applicationId`, not `namespace`), iOS `com.example.habitLoop` (Runner, Release — not `RunnerTests`), platform `both` |
| `expo-sample/` | `analytics` (@react-native-firebase/analytics), `crash` (@sentry/react-native), `location` (expo-location + its permission option). **Not** crashlytics: dev dependency | name `Tide Times`, Android + iOS `com.example.tidetimes` from `app.config.ts` production branch; `.dev` = `development`/`preview` EAS profiles → build types, excluded; native folders gitignored → config is the truth; `slug` not proposed as the address |
| `kmp-sample/` | `account` (dev.gitlive:firebase-auth, commonMain → both), `crash` (sentry-kotlin-multiplatform, commonMain → both), `location` (play-services-location + `ACCESS_FINE_LOCATION`, **Android only**); `NSMotionUsageDescription` as an iOS permission fact | name `PaceMate`, Android `com.example.pacemate` from `composeApp` (the `com.android.application` module, not `namespace`), iOS `com.example.pacemate.ios` resolved `${BUNDLE_ID}${TEAM_ID}` → `Config.xcconfig`; `desktop` target ignored; platform `both` |

Every sample uses a `com.example.*` id on purpose: the skill must **flag** it as not uploadable to
Google Play and ask for the real id rather than build a store URL from it. `flutter-sample` also
checks the case-only Android/iOS id difference, the zero AdMob id (no `app-ads.txt` line offered) and
the dev-dependency Sentry (mentioned, not a fact). Framework rules: `skills/store-ready/references/frameworks.md`.

Dry run (fresh Claude Code, plugin installed, `APPFOYER_API_KEY` exported):

```
cd plugin/examples/android-sample && claude
> make this app store-ready
```

Pass criteria (`docs/agent-path.md` §6): the skill prints the fact list with file:line, asks only
the questions in `references/questions.md`, ends with a review URL, and the site 404s until a
human publishes. Record the run in `docs/agent-path.md`.
