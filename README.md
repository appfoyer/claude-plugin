# AppFoyer plugin for Claude Code

Makes the mobile app in the current repository **store-ready**: your agent reads the build files,
derives what the app collects (SDKs, permissions), drafts the privacy policy, terms, support and
account-deletion pages plus `app-ads.txt` on AppFoyer, and hands you a review link. **You** read and
publish. The agent cannot publish anything.

## Install

```
export APPFOYER_API_KEY=cpk_…        # from app.appfoyer.com → Agent & API keys (shown once)
claude
/plugin marketplace add appfoyer/claude-plugin
/plugin install appfoyer@appfoyer
```

Then, inside your app's repository:

| Say | What happens |
|---|---|
| `Make this app store-ready` | scan → confirm facts → draft pages → review link |
| `Is this app store-ready?` | read-only audit against the compliance checklist |
| `Wire the store-ready links into the app` | after publishing: proposes the in-app privacy/support links as a diff |
| `Make com.acme.app.pro store-ready` | multi-flavor build: only the named flavors get a site |

**Multi-flavor builds:** a Gradle product flavor or Xcode target that ships under its own
application id is its own store listing, so it gets its own site — its own pages, its own
`/account-deletion` and its own `app-ads.txt`. The agent finds the flavors in the build files,
derives each one's SDKs and permissions separately (a free flavor's ad SDK never lands in the paid
flavor's privacy policy) and asks which ones to set up. Name the package ids in your prompt and it
skips the question. Build types (`debug`, `staging`) never get a site.

## What leaves your machine

Only **derived facts**: the app name, platform, bundle/application id, the names of SDKs and
permissions found, and the answers you confirm (company name, support email, jurisdiction, store
URLs). Never file contents, never source code, never secrets. The API key travels only in the
`Authorization` header that Claude Code fills from `APPFOYER_API_KEY`; the skill never reads it.

Revoke the key at any time in the dashboard; the next call fails immediately.

Staging or self-hosted endpoint: set `APPFOYER_MCP_URL` (default `https://app.appfoyer.com/mcp`)
before starting Claude Code, e.g. `export APPFOYER_MCP_URL=https://staging.appfoyer.com/mcp`.

## Guarantees

- Nothing goes public without your click on the review screen.
- Every page is moderated server-side at publish time, exactly like a manual edit.
- The pages are templates and drafts, **not legal advice**. You are responsible for what you publish.
