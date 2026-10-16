# AppFoyer plugin for Claude Code

Makes the mobile app in the current repository **store-ready**: your agent reads the build files,
derives what the app collects (SDKs, permissions), drafts the privacy policy, terms, support and
account-deletion pages plus `app-ads.txt` on AppFoyer, and hands you a review link. **You** read and
publish. The agent cannot publish anything.

## Install

```
claude
/plugin marketplace add appfoyer/claude-plugin
/plugin install appfoyer@appfoyer
```

There is no key to create or copy, and no account is needed to start: the agent reads your build,
shows what it found and asks its questions first, all on your machine. It asks you to connect only
when there is something to save. The first time the agent calls AppFoyer, your browser opens on
`app.appfoyer.com`: sign in (or create a free account), check the name and where the browser returns
to, and approve. Claude Code keeps the connection and renews it on its own. If the browser does not
open, run `/mcp`, pick **appfoyer** and choose **Authenticate**.

Claude Code does not refresh a third-party marketplace on its own. To get new versions, run `/plugin`,
open **Marketplaces**, pick **appfoyer** and choose **Enable auto-update** (or update by hand with
`claude plugin update appfoyer@appfoyer`).

Then, inside your app's repository:

| Say | What happens |
|---|---|
| `Make this app store-ready` | scan → confirm facts → draft pages → review link |
| `Is this app store-ready?` | read-only audit against the compliance checklist |
| `Wire the store-ready links into the app` | after publishing: proposes the in-app privacy/support links as a diff |
| `Make com.acme.app.pro store-ready` | multi-flavor build: only the named flavors get a site |
| `Add the AppFoyer drift check` | proposes a GitHub workflow that flags pull requests adding an SDK or permission the pages may not cover |

**Icon and address:** the agent also finds the app's own launcher/store icon in the repository
(Play 512 icon, the largest `mipmap` raster, or the iOS `AppIcon` marketing image) and uploads it,
and on Pro / Advanced claims a custom address built from the app's English name
(`weather-now.appfoyer.page`). The address passes the same brand and reserved-word checks as the
dashboard; if one is refused the agent tries the next candidate, at most five, then asks you. It
never replaces a custom address you already have without asking.

**Multi-flavor builds:** a Gradle product flavor or Xcode target that ships under its own
application id is its own store listing, so it gets its own site — its own pages, its own
`/account-deletion` and its own `app-ads.txt`. The agent finds the flavors in the build files,
derives each one's SDKs and permissions separately (a free flavor's ad SDK never lands in the paid
flavor's privacy policy) and asks which ones to set up. Name the package ids in your prompt and it
skips the question. Build types (`debug`, `staging`) never get a site.

## No browser on that machine?

A remote server, a container or a scripted run cannot open the approval page. Use an API key there
instead: create one at `app.appfoyer.com` → **Agent & API keys** → *API key — for CI and headless use*
(shown once), then add the server with the key in its header:

```
export APPFOYER_API_KEY=cpk_…
claude mcp add --transport http appfoyer-key https://app.appfoyer.com/mcp --header "Authorization: Bearer $APPFOYER_API_KEY"
```

A key reaches exactly what a browser connection does: drafts, never publishing.

## Other MCP clients

claude.ai, Claude Desktop, Cursor and VS Code can connect to `https://app.appfoyer.com/mcp` as a
remote (HTTP) MCP server, with the same approval page. They get the tools; the step-by-step workflow
that reads your build files is this plugin's skill, which runs in Claude Code.

## What leaves your machine

Only **derived facts**: the app name, platform, bundle/application id, the names of SDKs and
permissions found, and the answers you confirm (company name, support email, jurisdiction, store
URLs). Never source code, never secrets. The only file that leaves is the app's own icon, sent
once through a single-use upload URL. The connection's tokens are held by Claude Code, not by the
skill: the access token lives one hour and is renewed automatically, and the skill never reads it
(nor an API key, if you use one).

**One file in your repository:** `appfoyer.json`, a record of the SDKs, data types and permissions
the pages were written from. The plugin's bundled scanner (`bin/appfoyer-check.mjs`, build files only)
writes it at the end of each run; nothing in it is sent anywhere. Commit it, and the free GitHub
Action [`appfoyer/check-action`](https://github.com/appfoyer/check-action) flags every later pull
request that adds something the pages may not cover. The agent never commits or pushes.

Disconnect at any time on the dashboard's **Agent** page (the same list shows API keys, with
**Revoke**); the next call fails immediately.

Another endpoint (a staging environment): set `APPFOYER_MCP_URL` (default
`https://app.appfoyer.com/mcp`) before starting Claude Code; the approval page is then that
environment's.

## Guarantees

- No page goes public without your click on the review screen.
- The exceptions have no draft step, so on an app that is already published they change the live
  site at once — the agent tells you when it touched them:
  - the icon and the custom address;
  - `app-ads.txt` (sent only with publisher lines you confirmed);
  - settings the pages quote: app name, developer name, support email and store links.
- The agent cannot publish or unpublish anything, and cannot turn the account-deletion page off.
- Every page is moderated server-side at publish time, exactly like a manual edit.
- The pages are templates and drafts, **not legal advice**. You are responsible for what you publish.
