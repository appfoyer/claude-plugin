# Sample repos for the store-ready dry run

Two minimal repositories that contain **only the files the skill is allowed to read** (build,
manifest, plist, store metadata). No source code, so the skill's "derive, don't ask" rule can be
checked in isolation.

| Repo | Expected derived facts | Expected identity |
|---|---|---|
| `android-sample/` | `account` (firebase-auth), `analytics` (firebase-analytics), `crash` (firebase-crashlytics), `ads` (play-services-ads + `AD_ID`), `location` (`ACCESS_FINE_LOCATION`) | name `Trail Notes`, id `com.example.trailnotes`, platform `android`, Play URL proposed from the id |
| `ios-sample/` | `account` (FirebaseAuth + AuthenticationServices), `purchases` (RevenueCat), `crash` (Sentry), `location` (`NSLocationWhenInUseUsageDescription`) | name `Pocket Ledger`, bundle `com.example.pocketledger`, platform `ios`, App Store id `id1234567890` from `fastlane/Appfile` |

Dry run (fresh Claude Code, plugin installed, `APPFOYER_API_KEY` exported):

```
cd plugin/examples/android-sample && claude
> make this app store-ready
```

Pass criteria (`docs/agent-path.md` §6): the skill prints the fact list with file:line, asks only
the questions in `references/questions.md`, ends with a review URL, and the site 404s until a
human publishes. Record the run in `docs/agent-path.md`.
