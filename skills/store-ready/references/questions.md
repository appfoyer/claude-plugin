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

## Round 0 — flavors (only when the build has product flavors *and* the prompt named no package ids)

Ask this **before** Round 1: the answer decides how many apps exist and whose facts go on which
page. Skip it entirely when the developer's prompt already named the package ids — they have
answered it.

Show what you found first, one line per flavor:

```
This build ships 3 flavors:
- free        com.acme.weather.free        app/build.gradle:52 · src/free/AndroidManifest.xml
- pro         com.acme.weather.pro         app/build.gradle:57
- staging     com.acme.weather.debug       build type, not a store listing — excluded
```

| # | Question | Options |
|---|---|---|
| 0 | Which flavors should get their own store-ready site? (`multiSelect: true`) | one option per shipped flavor, labelled `<flavor> — <applicationId>`, all pre-selected · "Other" → the developer names a subset or a flavor you missed |

Then ask the display name only where it is not already answered: a flavor with its own
`app_name` / `CFBundleDisplayName` needs no question; otherwise offer `<base name> <Flavor>` as the
first option. Two apps must never end up with the same name — the developer cannot tell them apart
in the dashboard.

Rounds 1 and 2 then run **once** for the values that are the same across flavors (legal name,
support email, jurisdiction) and **per flavor** for the ones that are not: the store URL, the
publisher lines and any conditional question whose fact exists in one flavor and not another. Say
which flavor you are asking about in the question text.

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

- Never ask which flavor a fact belongs to — the source set and the dependency configuration say so.
- Legal name and support email are **always confirmed**, even with a good default — they end up
  in legal text.
- Jurisdiction is always asked; "Skip for now" keeps the template neutral.
- Do not ask about data retention, legal bases, children, or anything you would then have to
  *invent* answers for. Where the template needs such a value, write `[confirm: …]` in the draft —
  the developer resolves it on the review screen.
