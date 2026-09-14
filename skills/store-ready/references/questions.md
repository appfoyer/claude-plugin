# Asking the gaps

Show the derived fact list first — plain text, with the file and line for each fact:

```
I read the build files. Here is what I found (file → fact):
- android/app/build.gradle:41  play-services-ads            → ads
- android/app/build.gradle:38  firebase-analytics           → analytics
- AndroidManifest.xml:12       ACCESS_FINE_LOCATION         → location
```

Then ask with **`AskUserQuestion`**. Each call takes up to 4 questions; each question has 2–4
options plus an automatic "Other" for free text. Put the proposed default **first**. Ask only the
questions whose condition holds. Never ask for anything derived.

## Round 1 — identity (always)

| # | Question | Options |
|---|---|---|
| 1 | Are these facts complete and correct? | "Yes, all correct" · "Mostly — I'll correct below" · "Other" → the developer strikes or adds |
| 2 | Legal name shown on the policy | proposed value (from LICENSE / package author; `git config user.name` only when the repo root is the app itself) · "Other" |
| 3 | Support email | proposed value (README / LICENSE / plist / git user.email) · "Other" |
| 4 | Country whose law applies | as many guesses as the hints justify, 0–3 (store metadata locale, legal form like LLC/OÜ/GmbH, TLD of existing site) as 2-letter codes · "Skip for now" · "Other" |

## Round 2 — store and conditionals (only the rows that apply)

| # | Condition | Question | Options |
|---|---|---|---|
| 5 | Android detected | Play Store URL | proposed `https://play.google.com/store/apps/details?id=<applicationId>` · "Not listed yet" · "Other" |
| 6 | iOS detected | App Store URL | proposed `https://apps.apple.com/app/id<numeric id>` when an id was found · "Not listed yet" · "Other" |
| 7 | `ads` fact | app-ads.txt publisher lines | proposed AdMob line when the app id was found in the manifest/plist · "Later — I'll add them in the dashboard" · "Other" → paste lines |
| 8 | `purchases` fact | Products to describe in the terms | found product names/prices as one option ("Use these: …") · "Not applicable" · "Other" |
| 9 | `account` fact | How is sign-in used? | "Users create accounts" · "Anonymous / device sign-in only" · "Not used at runtime" — reorder so the option the README / entitlements support comes first |

If more than 4 rows apply in round 2, split into two calls; keep 5–6 together (three calls in total is then allowed).

## Fallback (agent without `AskUserQuestion`)

Ask the same items as a numbered list in one message, with the proposed default in parentheses,
and wait for the reply.

## Rules

- Legal name and support email are **always confirmed**, even with a good default — they end up
  in legal text.
- Jurisdiction is always asked; "Skip for now" keeps the template neutral.
- Do not ask about data retention, legal bases, children, or anything you would then have to
  *invent* answers for. Where the template needs such a value, write `[confirm: …]` in the draft —
  the developer resolves it on the review screen.
