---
name: store-ready
description: Make the mobile app in this repository store-ready on AppFoyer — derive data-collection facts from the build files, draft the privacy policy, terms, support and account-deletion pages plus app-ads.txt, and hand the developer a review link. Use when the developer says "make this app store-ready", asks for a privacy policy / account-deletion URL / app-ads.txt for their app, asks "is this app store-ready?", or asks to wire the published links into the app.
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
| "Make this app store-ready", "I need a privacy policy / deletion URL / app-ads.txt" | **Draft** (below) |
| "Is this app store-ready?", "what's missing for the store?" | **Audit**: steps 1–2, then `get_checklist` for the existing app (or say no app exists yet). No writes. |
| "Wire the store-ready links into the app", "add the privacy link to settings" | **Wire**: see the last section. Requires published pages. |

If a tool call fails with `missing_scope` or an HTTP 401, stop and tell the developer to create or
re-export an agent key at the connect URL in the error. Do not retry.

## Draft mode

### 1. Detect platform and identity — from files, never by asking

Read only build, manifest and store-metadata files: `build.gradle(.kts)`, `settings.gradle`,
`gradle/libs.versions.toml`, `AndroidManifest.xml`, `res/values/strings.xml`, `Podfile`, `Podfile.lock`,
`Package.swift`, `*.xcodeproj/project.pbxproj`, `Info.plist` and other `*.plist` config files,
`PrivacyInfo.xcprivacy`, `pubspec.yaml`, `package.json`, `app.json`, fastlane `Appfile`/`Deliverfile`
and `fastlane/metadata/**`, store listing texts (`appstore/`, `playstore/`, `metadata/`), `LICENSE`,
`README*`. Do not open source files, `.env*`, keystores or CI secrets.

Derive: platform, app name, application/bundle id, proposed Android store URL. Details:
`references/sdk-map.md`.

### 2. Derive data-collection facts

Map dependencies and permissions to `account | analytics | ads | crash | location | purchases | none`
with `references/sdk-map.md`. Keep the **file and line** for every fact.

### 3. Check for an existing app (re-runs)

Call `list_apps`. If an app matches by name or store URL, this is a **re-run**: call `get_app`,
diff your fact list against `dataCollection`, and continue with `update_app` + drafts for the
pages the diff touches only. Otherwise continue with a new app.

### 4. Show facts, then ask the gaps — interactively

Print the fact list with sources first (plain text, so the developer can read it). Then ask with
the **`AskUserQuestion` tool** — selectable options, proposed defaults pre-filled as the first
option, "Other" for free text — in at most two rounds of ≤ 4 questions each, exactly as laid out
in `references/questions.md`. If the tool is not available in this agent, fall back to the plain
numbered list in the same file. Wait for the answers. Nothing has been sent yet.

### 5. `create_app` (or `update_app`)

Send the confirmed values once. Outcomes:

- **`quota` present** → the app exists but cannot be published (plan limit). Say, in one sentence,
  that it was created unpublished and give `dashboardUrl`. **Keep drafting** — the drafts are
  useful once the developer upgrades or frees a slot.
- **`content_rejected`** → print every reason with its `field`, `matched` and `fix`, ask the
  developer what to change, and stop. Never retry with a tweaked name on your own.
- **`too_many_apps`** → tell the developer to delete an app in the dashboard; stop.

### 6. Draft the five pages with `set_page`

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
to add them (Dashboard → app-ads.txt). Mention that app-ads.txt hosting is **Beta**.

### 8. `request_publish` and stop

Print the `reviewUrl` and, verbatim: **"Nothing is public yet. Review each page and click
Publish."** Then print the paste table:

| Console field | URL |
|---|---|
| Google Play → App content → Privacy policy | `<site>/privacy` |
| Google Play → App content → Data deletion | `<site>/account-deletion` |
| App Store Connect → App Privacy → Privacy Policy URL | `<site>/privacy` |
| App Store Connect → App Information → Support URL | `<site>/support` |

Those fields live in the store consoles; you cannot fill them.

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
- Send file contents or source, run builds, or modify the repository in Draft/Audit mode.
- Claim a store will accept the pages, or that `app-ads.txt` is verified with any ad network.
- Present the drafts as legal advice.
