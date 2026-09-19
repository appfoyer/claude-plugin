---
name: store-ready
description: Make the mobile app in this repository store-ready on AppFoyer — derive data-collection facts from the build files, draft the privacy policy, terms, support and account-deletion pages plus app-ads.txt, and hand the developer a review link. Use when the developer says "make this app store-ready", asks for a privacy policy / account-deletion URL / app-ads.txt for their app, asks "is this app store-ready?", asks for a specific flavor or package id to be made store-ready, or asks to wire the published links into the app.
---

# Store-ready (AppFoyer)

You draft; the developer publishes. There is **no publish tool** and you must never try to work
around that. Everything you write lands in a draft slot and becomes public only when the developer
clicks Publish on the review screen. Say this plainly every time you hand off.

Only **derived facts** leave this machine: app name, platform, application/bundle id, SDK and
permission *names*, and the developer's confirmed answers. Never send file contents, source code,
secrets, keystores or `.env` values. Never read or print `APPFOYER_API_KEY`.

## Which mode

| Developer says | Mode |
|---|---|
| "Make this app store-ready", "I need a privacy policy / deletion URL / app-ads.txt", "…for com.acme.app.pro" | **Draft** (below) |
| "Is this app store-ready?", "what's missing for the store?" | **Audit**: steps 1–2 (including flavor detection, 1b), then `get_checklist` for each matching existing app — a flavor with no app is itself the finding. No writes. |
| "Wire the store-ready links into the app", "add the privacy link to settings" | **Wire**: see the last section. Requires published pages. |

If a tool call fails with `missing_scope` or an HTTP 401, stop and tell the developer to create or
re-export an agent key at the connect URL in the error. Do not retry.

A JSON-RPC **validation error** (`-32602`, e.g. "Too big … <=102400" on `markdown`) means nothing
was written. Treat it like a `too_large` result: shorten the draft and re-send once.

## Draft mode

### 1. Detect platform and identity — from files, never by asking

Read only build, manifest and store-metadata files: `build.gradle(.kts)`, `settings.gradle`,
`gradle/libs.versions.toml`, `AndroidManifest.xml`, `res/values/strings.xml`, `Podfile`, `Podfile.lock`,
`Package.swift`, `*.xcodeproj/project.pbxproj`, `Info.plist` and other `*.plist` config files,
`*.entitlements`, `PrivacyInfo.xcprivacy`, `*.storekit`, `pubspec.yaml`, `package.json`, `app.json`, fastlane `Appfile`/`Deliverfile`
and `fastlane/metadata/**`, store listing texts (`appstore/`, `playstore/`, `metadata/`), `LICENSE`,
`README*`. Do not open source files, `.env*`, keystores or CI secrets.

Derive: platform, app name, application/bundle id, proposed Android store URL, **and the build's
product flavors** (Gradle `productFlavors`, Xcode targets/schemes with their own bundle id). Details:
`references/sdk-map.md`.

### 1b. Flavors — one shipped flavor is one app

A flavor that ships to a store under its **own application/bundle id** is its own store listing, so
it needs its own pages, its own `/account-deletion` and its own `app-ads.txt` — one AppFoyer app per
flavor. Build types (`debug`, `staging`, an `applicationIdSuffix` on a non-shipped variant) are
**not** flavors: never make an app for one.

- **The developer's prompt named package ids** ("make the pro and enterprise flavors store-ready",
  `com.acme.app.pro com.acme.app.lite`) → use exactly those and ask nothing about the selection.
  An id that matches no flavor in the build is an error: say which ids you found and stop. Do not
  substitute the closest one.
- **Flavors found, none named** → list them (flavor name, application id, source) and ask which to
  make store-ready with the multi-select question in `references/questions.md` (Round 0). The
  default selection is every flavor with a distinct application id that is not a debug/staging
  variant.
- **No flavors** → one app, exactly as before. Do not ask the question.

Facts are derived **per flavor**: shared facts (the `main` source set and plain `implementation`
lines) plus that flavor's own (`<flavor>Implementation` / `<flavor>Api` dependencies,
`src/<flavor>/AndroidManifest.xml`, `src/<flavor>/res/values/strings.xml`, its `.xcconfig` or
target-level settings). A free flavor's ad SDK is a fact about the free flavor only — never write it
into the paid flavor's privacy policy. Each flavor also gets its own proposed store URL, built from
its own application id.

### 2. Derive data-collection facts

Map dependencies and permissions to `account | analytics | ads | crash | location | purchases | none`
with `references/sdk-map.md`. Keep the **file and line** for every fact. Usage-description keys
and permissions with no `dataCollection` bucket (Face ID, camera, notifications…) are still
permission facts — keep them with file:line for the privacy page's permissions section. README
prose ("sign in with Google") is a hint for question defaults, never a fact on its own.

### 3. Check for an existing app (re-runs)

Call `list_apps`. Match **per selected flavor**, in this order: the application id inside the Play
URL (`…details?id=<applicationId>`), then the app name. A match is a **re-run** for that flavor:
call `get_app`, diff your fact list against `dataCollection`, and continue with `update_app` +
drafts for the pages the diff touches only. Unmatched flavors are new apps. A flavor set is often
half and half — re-run two, create one — so decide this one flavor at a time, never for the batch.

### 4. Show facts, then ask the gaps — interactively

Print the fact list with sources first (plain text, so the developer can read it). Then ask with
the **`AskUserQuestion` tool** — selectable options, proposed defaults pre-filled as the first
option, "Other" for free text — in at most two rounds of ≤ 4 questions each, exactly as laid out
in `references/questions.md`. If the tool is not available in this agent, fall back to the plain
numbered list in the same file. Wait for the answers. Nothing has been sent yet.

### 5. `create_app` / `create_apps` (or `update_app`)

Send the confirmed values once. One new app → `create_app`. Two or more (a flavor set) →
**`create_apps`**, up to 10 per call, one entry per flavor with that flavor's own name, facts and
store URLs; split a longer set across calls. Every app is created unpublished, scored and
limit-checked on its own.

`create_apps` answers with `created`, `failed` and `skipped` — report all three. A failed entry
never discards the others: keep drafting for what was created, show every reason for what was not.
If the result carries `publishLimit`, the plan has fewer free slots than the flavors you just
created: say the numbers plainly (`remaining` of `limit`), keep drafting all of them, and let the
developer choose which to publish or upgrade. Never drop a flavor on your own to fit the plan.

Outcomes per entry:

- **`quota` present** → the app exists but cannot be published (plan limit). Say, in one sentence,
  that it was created unpublished and give `dashboardUrl`. **Keep drafting** — the drafts are
  useful once the developer upgrades or frees a slot.
- **`content_rejected`** → print every reason with its `field`, `matched` and `fix`, ask the
  developer what to change, and stop. Never retry with a tweaked name on your own.
- **`too_many_apps`** → tell the developer to delete an app in the dashboard; stop.

### 6. Draft the five pages with `set_page`

With several flavors, do this for **each app in turn**, with that flavor's own fact list, name and
store URLs. Never reuse one flavor's draft for another by search-and-replacing the name: the data
facts, the permissions and the store links are what differ, and they are the content that matters.

Start from the **template text** in `get_app` → `pages[*].markdown` (it already contains the
disclaimer block and the `{{variables}}` filled from Settings — keep both). Edit it into a page
about *this* app:

- Privacy: list each provider found in step 2 by name with its purpose and a link to the
  vendor's privacy policy; state the permissions and why the app needs them; keep the
  account-deletion section pointing at the app's own `/account-deletion`.
- Terms: purchases/subscriptions only if the fact exists and the developer answered question 7;
  keep the "not legal advice / your responsibility" clause.
- Support: the confirmed support email and the store URLs; no other contact addresses.
- Account deletion: keep the request-form wording; describe what deleting an account does *in
  this app* only if the developer told you.
- Home: one paragraph, the store badges (declared store URLs only).

Constraints, verbatim: **Markdown only, no HTML, no images, no forms.** Links: only the app's own
site, `mailto:` to the declared support email, the declared store listings and well-known SDK
policy pages are clickable — everything else is printed as text, so don't add them. Never write
"Download" labels pointing outside the stores. Do not invent retention periods, legal bases,
company addresses or DPO names — write `[confirm: …]` and move on.

`set_page` returns `advisory.reasons`. If non-empty, fix what each `fix` says and re-send **once**.
If a reason remains, leave it — the developer will see it on the review screen.

### 7. `set_ad_lines` — only with an ads fact and publisher lines

If the developer gave publisher lines, send them as-is. If they said "later", skip and say where
to add them (Dashboard → app-ads.txt). Remind them to set the site as the developer website in the
store listing — AdMob crawls the hostname of that URL.

### 8. `request_publish` and stop

Print the `reviewUrl` and, verbatim: **"Nothing is public yet. Review each page and click
Publish."** Then print the paste table:

| Console field | URL |
|---|---|
| Google Play → App content → Privacy policy | `<site>/privacy` |
| Google Play → App content → Data deletion | `<site>/account-deletion` |
| App Store Connect → App Privacy → Privacy Policy URL | `<site>/privacy` |
| App Store Connect → App Information → Support URL | `<site>/support` |

Those fields live in the store consoles; you cannot fill them. With several flavors, call
`request_publish` for each app and print one table **per flavor**, headed by its application id —
the consoles are per listing, and pasting one flavor's URLs into another's listing is a store
rejection waiting to happen.

Then offer one last choice with `AskUserQuestion` (or plain text): **"Show me each draft here"**
or **"I'll review in the dashboard"**. On the first, print every draft's Markdown in full, one
page per message, and repeat the review URL at the end. Publishing itself always happens on the
review screen — it shows the rendered page next to the live one with the moderation result, which
a terminal cannot. Do not open a browser, do not poll, do not call anything else.

## Wire mode (after publishing)

1. `list_apps` → `get_app`. If the pages you need are not `published`, stop and point at the review URL.
2. Find where the app already shows or should show a privacy / terms / support link: an
   About/Settings screen, `strings.xml` / `Localizable.strings` URL constants, an existing
   hard-coded policy URL, the store metadata files under `fastlane/metadata`.
3. Propose a diff. Apply it **only on an explicit yes**. Never commit, never push.

## Never

- Publish, or ask the developer for the key so you can "do it manually".
- Create an app for a build type (`debug`, `staging`) or for a flavor the developer did not select.
- Share one flavor's facts, pages or store links with another flavor.
- Send file contents or source, run builds, or modify the repository in Draft/Audit mode.
- Claim a store will accept the pages, or that `app-ads.txt` is verified with any ad network.
- Present the drafts as legal advice.
