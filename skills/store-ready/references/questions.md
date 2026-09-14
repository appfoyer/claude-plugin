# The one message of questions

Ask everything the code cannot tell in a single message, with proposed defaults, **after** showing
the derived fact list with sources. Never ask for anything listed as "derived".

```
I read the build files. Here is what I found (file → fact):
- android/app/build.gradle:41  play-services-ads            → ads
- android/app/build.gradle:38  firebase-analytics           → analytics
- AndroidManifest.xml:12       ACCESS_FINE_LOCATION         → location
Add or strike anything that is wrong.

A few things the code cannot tell me:
1. Legal name shown on the policy (proposed: "Acme Ltd" from LICENSE) —
2. Support email (proposed: support@acme.example from README) —
3. Country whose law applies, 2 letters (e.g. DE, US, AZ), or "skip" —
4. Play Store URL (proposed: https://play.google.com/store/apps/details?id=com.acme.weather — is it listed yet?) —
5. App Store URL (numeric id needed; leave blank if not on iOS or not listed) —
6. Since the app shows ads: your app-ads.txt publisher lines (e.g. from AdMob → "Manage app-ads.txt"), or "later" —
7. Since the app has purchases: subscription names, prices, periods, trial, and how to cancel — or "not applicable" —
```

Rules:

- Legal name and support email are **always confirmed**, even with a good default — they end up in
  legal text.
- Jurisdiction is always asked; "skip" is allowed and the template stays neutral.
- Questions 6 and 7 appear only when the matching fact exists.
- Do not ask about data retention, legal bases, children, or anything you would then have to
  *invent* answers for. Where the template needs such a value, write `[confirm: …]` in the draft —
  the developer resolves it on the review screen.
