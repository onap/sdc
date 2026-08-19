# SDC Playwright E2E Tests

Playwright end-to-end tests for the SDC frontend, running against the same integration-test
Docker stack as the Selenium/TestNG suite (Cassandra, backend, onboarding backend, frontend,
webseal-simulator).

**Why this suite exists.** `catalog-ui` is mid-migration from AngularJS 1.6 to Angular
(epic SDC-4829). Every regression it has shipped so far was **green in Jest, green in AOT and
green in Selenium**: a `$scope.save()` left as a no-op shim, dropdowns bound to a property that
exists nowhere, top-nav buttons that updated the URL but not the view, CSS lost to a deleted
`@import`. Those all need a real browser plus an assertion on the *effect* (the PUT body, the
swapped view element) rather than on the UI's appearance. That is the gap these specs fill.

---

## TL;DR — the commands that work

Assuming images are built and the stack is up (see [Starting the stack](#starting-the-stack)):

```bash
cd integration-tests/playwright-tests
npm ci && npx playwright install chromium           # first time only
SDC_BASE_URL=http://localhost:8285 npx playwright test --reporter=list
```

Expected: **31 passed**, ~3.5 minutes, zero retries. Anything less is a real finding — read
[Triage](#triage-when-tests-fail) before touching a spec, because most red runs here have been
environmental, not test bugs.

`SDC_BASE_URL` is **not** optional in practice. It defaults to `http://localhost:8285` in
`playwright.config.ts`, which happens to be right for the Docker stack, but stating it makes the
target explicit and is required for the dev-server path.

---

## Prerequisites

- Node.js 18+ (Playwright ≥ 1.42 requires it)
- Docker with Compose v2
- ~6 GB RAM for the containers
- Free host ports (the full set the fabric8 stack binds): 4000, 4001, 4444, 5000, 5900, 6000,
  8080, 8081, 8085, 8181, 8285, 8286, 8443, 8445, 9042, 9443
- ONAP `settings.xml` at `~/.m2/settings.xml` (only for the Maven build steps)

## Starting the stack

### 1. Build the Docker images (one-time, ~15 min)

```bash
# from the repo root
mvn clean install -P all,docker -DskipTests
```

`-P all` keeps the default module list active when another profile is named. `clean` is
required: the `build-helper-maven-plugin` `parse-version` goal is bound to `pre-clean`, and
without it the `${parsedVersion.*}` variables in the Docker image tags do not resolve.

This step builds images only. It used to start and immediately stop the whole stack as well,
because the `docker:start`/`docker:stop` executions sit in the default `<build>` of
`integration-tests`; they are now gated on `it.stack.disabled`, which defaults to `true` and is
flipped by the `run-integration-tests-*`, `all-for-integration-tests-only`, `start-sdc` and
`stop-sdc` profiles. Set `-Dit.stack.disabled=false` if you want the old behaviour back.

### 2. Start the containers

```bash
mvn pre-integration-test -P run-integration-tests-playwright -f integration-tests/pom.xml
```

`pre-integration-test` starts the containers but neither runs the tests nor tears them down, so
the stack stays up for iterating. This is the `io.fabric8:docker-maven-plugin` stack, so
containers are named with a **`-1` suffix** (`sdc-frontend-1`, `sdc-backend-all-plugins-1`, …),
they carry **no Compose labels**, and they join the `sdc-network` bridge.

> There is also a Compose file at `.claude/skills/sdc-local-dev/docker-compose.yml` which starts
> an equivalent stack with **unsuffixed** names (`sdc-FE`, `sdc-BE`, `sdc-sim`). Pick one and stay
> with it; the two cannot coexist, as both bind the same host ports and the same network aliases.

### 3. Wait for the readiness gate

First start takes **4–6 minutes** — the bulk of it is importing ~97 normative types. The gate that
actually matters is `sdc-BE-init` reaching `Done` (fabric8 waits up to 660 s on that log line). A
backend that is up but has not finished importing normatives will fail asset creation with
confusing category errors, so do not start testing early.

The init containers stream their stdout into the Maven output (`docker.showLogs` names them), so
the import progress — and the type it dies on, if it does — is in the build log itself. Note that
setting `-Ddocker.showLogs=false` does not narrow that: `false` suppresses logs for *every* image,
per-image `<log>` configuration included.

```bash
curl -s http://localhost:8080/sdc2/rest/healthCheck | python3 -m json.tool
```

`BE`, `JANUSGRAPH` and `CASSANDRA` must all be `UP`. **DMAAP and DE will be DOWN — that is
expected** on a local stack and does not affect any of these tests.

Then confirm the simulator, which is what `SDC_BASE_URL` points at:

```bash
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:8285/login   # expect 200
```

### Stopping

```bash
mvn docker:stop -f integration-tests/pom.xml
```

## Running via Maven (full lifecycle)

One command to start Docker, install Node/npm, install the browser, run the tests and tear
Docker down again:

```bash
mvn verify -P run-integration-tests-playwright -f integration-tests/pom.xml
```

Assumes the images already exist locally (step 1). Reports:

| Artifact             | Path                                                      |
| -------------------- | --------------------------------------------------------- |
| HTML report          | `integration-tests/target/playwright-report/index.html`   |
| JUnit XML            | `integration-tests/target/playwright-reports/results.xml` |
| Screenshots & traces | `integration-tests/target/playwright-results/`            |

`trace: 'on'` is set unconditionally in `playwright.config.ts`, so every run leaves a trace.
`npx playwright show-trace <zip>` is by far the fastest way to diagnose a failure — pointed at a
single trace zip out of `playwright-results/`, **not** at a whole report bundle (see CI below).

---

## Triage: when tests fail

Work down this list **before** editing a spec. Every entry below is a failure actually hit while
building this suite, in descending order of how much time it costs to misdiagnose.

### 1. Is the deployed image actually built from your HEAD?

**This produced 9 simultaneous failures once, all phantoms.** The stack was serving an FE image
built weeks earlier; the tests were faithfully reproducing regressions that had *already been
fixed* in the working tree. Symptoms look exactly like real regressions, so check this first
whenever more than two unrelated tests fail together.

```bash
docker images --format '{{.Repository}}:{{.Tag}}\t{{.CreatedAt}}' | grep sdc-frontend | head -3
git log -1 --format='HEAD %h %cd'
```

If the image predates the commits you expect to be testing, rebuild the frontend (below). Then
verify the **served** bundle rather than the image — a stale container can serve old code under a
fresh tag:

```bash
curl -s http://localhost:8181/ | grep -o 'main\.[0-9]*\.bundle\.js'
curl -s "http://localhost:8181/scripts/main.<stamp>.bundle.js" | gunzip 2>/dev/null | grep -c '<symbol you expect>'
```

### 2. Rebuilding just the frontend

The FE container **must** join `sdc-network` with the network alias `sdc-FE`. The simulator
proxies to the host name `sdc-FE:8181`, so without the alias every `/sdc1/feProxy/...` call
through port 8285 returns `500 UnknownHostException: sdc-FE` — and the whole suite goes red at
login.

```bash
# from the repo root, after editing catalog-ui/
(cd catalog-ui && npx webpack --config webpack.production.js)
cp -r catalog-ui/dist/. catalog-fe/src/main/webapp/
mvn package -pl catalog-fe -DskipTests -Dcheckstyle.skip -Djacoco.skip=true -DskipPMD -q
cp catalog-fe/target/catalog-fe-*-SNAPSHOT.war catalog-fe/sdc-frontend/
mvn process-resources docker:build -pl catalog-fe -P docker -DskipTests -Ddocker.noCache=true -q

docker rm -f sdc-frontend-1                     # the name is NOT reused automatically
docker run -d --name sdc-frontend-1 --network sdc-network --network-alias sdc-FE \
  -e ENVNAME=AUTO -e FE_HOSTNAME=sdc-frontend-1 \
  -e BE_HOSTNAME=sdc-backend-all-plugins-1 -e BE_PORT=8443 \
  -e ONBOARDING_BE_HOSTNAME=sdc-onboard-backend-1 -e ONBOARDING_BE_PORT=8445 \
  onap/sdc-frontend:latest
```

The `docker rm -f` is not optional: `docker run` fails on the existing name, and it is easy to
read that error as a build failure.

Everything written into `catalog-fe/src/main/webapp/` and `catalog-fe/sdc-frontend/*.war` is a
build byproduct and is already covered by `.gitignore` (`catalog-fe/src/main/webapp/*`, `*.war`).
Never stage it.

### 2b. Hot-swapping one bundle into the running FE serves the `.jsgz`, not the `.js`

Handy for proving a CSS-only fix red-then-green without a 15-minute rebuild — but the FE ships a
pre-gzipped sibling of every bundle and jetty serves *that* to any client sending
`Accept-Encoding: gzip`, i.e. every browser. Patch only `styles.<hash>.bundle.js` and `curl` will
confirm your fix while the browser keeps loading the stale `styles.<hash>.bundle.jsgz` — the test
stays red and the served asset looks correct, which reads as a caching problem and is not one.
Patch both, in **both** exploded-war dirs (jetty keeps more than one under `/tmp/jetty-*catalog-fe*`):

```bash
docker exec sdc-frontend-1 sh -c 'ls -d /tmp/jetty-*catalog-fe*/webapp/scripts'
gzip -c -9 patched.js > patched.jsgz      # then docker cp each variant over its counterpart
```

Confirm from inside the page rather than from `curl` — walk `document.styleSheets` for the rule.
Restore the originals afterwards; a patched container silently invalidates every later run.

### 3. Known-benign console noise

These appear on a healthy local stack. Do not chase them: the ADMIN-tab `RangeError`, "AngularJS
injector before it being set", `NsProfile` ui-model warnings, and `404 /wf/workflows` (sdc-wfd is
not part of this stack).

### 4. Empty catalog / empty dashboard

Not necessarily a regression — the local stack starts with no user-owned assets. Every spec that
needs data creates it through the REST API (`api.createVf()` / `api.createService()`), so an
empty *list* only matters if a spec depended on it. Check `/rest/v1/followed` before concluding
anything.

---

## Fast iteration with webpack-dev-server

Rebuilding the FE image per change costs ~3 minutes; the dev server costs ~2 seconds. The same
specs run against both targets; the fixture detects which one via `IS_DEV_SERVER` and compensates
for the two dev-server gaps below. One test — the simulator's user-table check — `test.skip`s on
the dev server, which serves its own minimal `/login` with no such table. So expect
**23 passed / 1 skipped** here versus **24 passed** against Docker.

Restarting is required after editing `webpack.server.js`: HMR reloads application code, not the
dev-server config.

```bash
cd catalog-ui
npm install     # first time only
SDC_BACKEND_HOST=localhost SDC_BACKEND_PORT=8080 SDC_BACKEND_PROTOCOL=http npm start
```

```bash
cd integration-tests/playwright-tests
SDC_BASE_URL=http://localhost:9000 npx playwright test
```

`SDC_DEV_PORT` moves the dev server off 9000, which is what lets a baseline and a patched build run
side by side — the only way to tell a real regression from a pre-existing console error. `IS_DEV_SERVER`
recognises :9000-:9009, so stay inside that range or the plugins stub is skipped and a dozen
specs fail on a click-blocking modal instead of on anything real.

```bash
SDC_DEV_PORT=9001 SDC_BACKEND_HOST=localhost npm start        # in catalog-ui
SDC_BASE_URL=http://localhost:9001 npx playwright test
```

### Pair the port with `SDC_DIRECT_FE` correctly, or every API call 404s

`webpack.server.js:83` rewrites `^/sdc1/feProxy/rest` → `/sdc2/rest` **only when
`SDC_DIRECT_FE` is not `true`**. So there are two valid configurations and two broken ones:

| `SDC_BACKEND_PORT` | `SDC_DIRECT_FE` | Rewrite | Result |
| --- | --- | --- | --- |
| 8080 (BE) | unset/false | on → `/sdc2/rest` | ✅ 200 |
| 8181 (FE) | `true` | off → `/sdc1/feProxy/rest` | ✅ 200 |
| **8181 (FE)** | **unset/false** | on → `/sdc2/rest` | ❌ **404 — the FE has no `/sdc2/rest`** |
| 8080 (BE) | `true` | off | ❌ 404 — the BE has no `/sdc1/feProxy` |

Verified against this stack; the two ✅ rows return 200 and the two ❌ rows return 404. The
default (`SDC_BACKEND_PORT=8080`, `SDC_DIRECT_FE` unset) is the one shown above.

The failure symptom is thoroughly misleading: the app loads, then immediately lands on
`#!/error-403` with `AUTH FAILED! from app module` in the console. It looks like a broken cookie
or auth service. It is not — a bad `USER_ID` returns 403, not 404, and several unrelated
endpoints 404 together. Verify the proxy through the dev server before browser-testing:

```bash
curl -s -o /dev/null -w '%{http_code}\n' -H 'USER_ID: cs0008' \
  http://localhost:9000/sdc1/feProxy/rest/v1/user/authorize     # must be 200
```

### The dev server's "Not Found" modal blocks clicks

Any 404 raises an SDC error modal, and its `.modal-background` backdrop then intercepts **every
subsequent click**. The failure is maximally misleading: an opaque click timeout on a locator
Playwright cheerfully reports as *"visible, enabled and stable"*, with the real cause a request
that failed seconds earlier and several DOM layers away. Two dev-server-only 404s cause it:

| Failing call | Why | Fix |
| --- | --- | --- |
| `/sdc1/feProxy/uicache/v1/catalog` | `uicache` is a **catalog-fe** concept. `FeProxyServlet` rewrites it to the BE's `rest/v1/screen`; nothing did that when the dev server targets the BE directly, so the CATALOG page 404'd. | Fixed at source in `catalog-ui/webpack.server.js` — the proxy now mirrors the servlet's mapping. Verified: BE `/sdc2/rest/v1/screen` returns the identical payload (same keys, 178 resources / 31 services). |
| `/sdc1/feProxy/uicache/v1/catalog/resources/latestversion/notabstract/metadata` | The composition **left palette**. `FeProxyServlet:242-246` maps this one path to `rest/v1/**catalog**`, not `rest/v1/screen` — the same facade prefix, a different target. | Same fix, but as a **rule that must be listed first**: see the ordering note below. |
| `/sdc1/rest/config/ui/plugins` | catalog-fe serves this from its own `plugins-configuration.yaml`; **the BE does not serve it at all** — every `/sdc2/rest/**/plugins` spelling returns 500, so a rewrite would turn a 404 into a 500. | Stubbed in the fixture (`stubPluginsConfig`, empty array), dev-server only. |

**`pathRewrite` applies the FIRST matching rule only** — `http-proxy-middleware`'s
`path-rewriter.js` returns `false` from its `forEach` on the first hit. The palette URL *starts
with* `/uicache/v1/catalog`, so with the rules in the other order it is rewritten to
`/v1/screen/resources/latestversion/notabstract/metadata`, which the BE answers **500**, and the
error modal's backdrop then blocks every click on the composition page. Verify all three after any
change to that block:

```bash
for p in \
  '/sdc1/feProxy/uicache/v1/catalog/resources/latestversion/notabstract/metadata?internalComponentType=VF' \
  '/sdc1/feProxy/uicache/v1/catalog' \
  '/sdc1/feProxy/uicache/v1/followed' ; do
  curl -s -o /dev/null -w "%{http_code} $p\n" -H 'USER_ID: cs0008' "http://localhost:9000$p"
done   # all three must be 200
```

`stubPluginsConfig` is registered by the `sdcPage` fixture **before** `login()`, because the
request fires during bootstrap — a route added afterwards misses it. Note that dismissing the
modal after login is *not* sufficient for the uicache case: that request fires lazily on first
CATALOG navigation, so the modal reappears mid-spec. Prefer removing the 404 to racing its modal.

`dismissTransientModal(page)` remains available for specs that legitimately open a modal mid-flow.

### `npm start` reaped with exit 144

In some agent/harness sessions, launching webpack-dev-server as a backgrounded shell job (`&`,
`nohup`, even `setsid`) gets its whole process group killed when the launching call returns. Use
the harness's own background-run mechanism, then leave it strictly alone: poll readiness with
read-only `curl http://localhost:9000` in *separate* commands, and never `pkill`/restart it
mid-wait — issuing a process-touching command is what triggers the reap. The JIT dev bundle takes
60–120 s to compile; wait before the first probe.

`npm start` and `npm run start:local` are **the same command** (`webpack-dev-server`); the
distinction in older docs was never real.

### Pointing at a remote backend

```bash
cd catalog-ui
SDC_BACKEND_HOST=<host> SDC_BACKEND_PORT=443 SDC_BACKEND_PROTOCOL=https SDC_DIRECT_FE=true npm start
```

---

## Suite layout

Import `test` from the fixtures module, **never** from `@playwright/test` directly — the local
`test` is extended with the fixtures below.

```ts
import { test, expect, SEL, settles, gotoWorkspaceTab } from './fixtures/sdc';

test('...', async ({ sdcPage, api }) => { ... });
```

| File | Guards |
| --- | --- |
| `tests/fixtures/sdc.ts` | fixtures, selectors, settle detection, navigation, REST asset creation |
| `tests/sdc-sanity.spec.ts` | simulator login, `#!` hash prefix on the landing URL |
| `tests/workspace-shell.spec.ts` | shell chrome, CREATE vs VIEW/EDIT mode, EXACT-class Selenium xpath contracts |
| `tests/workspace-navigation.spec.ts` | top-nav view swap, deep-link, reload, BACK/FORWARD, unknown-route fallback, sidebar |
| `tests/general-tab-save.spec.ts` | the silent-data-loss PUT, Service Role / Service Function controls |
| `tests/interface-tabs.spec.ts` | downgraded interface components receive their `component` input |
| `tests/unsaved-changes.spec.ts` | the dirty-form navigation guard: warns, blocks, then releases on OK |
| `tests/composition-geometry.spec.ts` | composition's `bodyClass`-driven full-bleed layout |
| `tests/workspace-gutter.spec.ts` | the tab content gutter vs the sidebar, and top-bar overflow |
| `tests/workspace-tab-title.spec.ts` | every titled tab's heading is visible and not painted over (SDC-4879) |
| `tests/workspace-topbar-controls.spec.ts` | the 5 top-bar controls the AngularJS removal dropped: they render, are hit-testable, and reach the BE |

### Fixtures

- **`sdcPage`** — a page already logged in as `cs0008` (DESIGNER) with the shell rendered, and
  the dev-server modal dismissed if applicable.
- **`api`** — an `SdcApi` over Playwright's `APIRequestContext` for creating assets out of band.
  Deliberately *not* the page's `$http`: an in-page POST adds a pending request that the settle
  predicate then blocks on, which is what made the earlier specs racy. It also survives CR 3,
  since there is no AngularJS injector left to reach.

  `createVf()` / `createService()` **poll until the new asset reads back** before returning.
  JanusGraph/Cassandra gives no read-after-write guarantee across the BE's transactions, so a `201`
  is sometimes followed within the same second by `404`/`SVC4063` on the very `uniqueId` it just
  returned (observed on the 2026-08-14 run: `GET .../catalog/resources/<id>/filteredDataByParams`).
  The UI then bounces to the dashboard behind an error modal and the spec fails on a missing sidebar
  — which reads as a UI regression and is not one. `retries: 1` made it worse, not better: the
  retry passed, so the run reported all-green and the flake showed up only in the HTML report, never
  in the JUnit XML the gate votes on. If a red run looks like "the workspace did not render",
  check the network log for a 404 on the asset before suspecting the UI.

  The gate has since fired for real (CI run 31798342250): one VF stayed 404 for the whole 30 s
  budget while the retry's freshly created asset read back immediately. So the message means *this
  write did not land*, not *the BE is slow* — a new asset is the way out, which is what `retries: 1`
  now buys, and the run stays green with a visible `1 flaky` instead of a mystery failure.

### Selectors are contracts, not conveniences

`SEL` in `fixtures/sdc.ts` is the single source of truth. Most entries are **also** depended on
by Selenium page objects and/or Cypress specs, so changing one breaks three suites. Two in
particular:

- `mainRightContainer` / `topBar` are matched by **exact-class** xpath
  (`//div[@class='%s']`, no `contains()`) in 8 Selenium page objects — 7 for
  `w-sdc-main-right-container`, plus `ResourceWorkspaceTopBarComponent` for
  `sdc-workspace-top-bar` and `sdc-workspace-top-bar-buttons`. Adding *any* class to those
  elements breaks every one of them. `workspace-shell.spec.ts` asserts this.
- `chromeCreateButton` vs `formSaveButton`: both carry `data-tests-id="create/save"`, so an
  unscoped selector is ambiguous. Always scope by the wrapper class.

### Settle detection — and why it is not synchronisation

`settles(page)` derives from `AdditionalConditions.pageLoadWait()` in the Selenium suite, minus the
AngularJS branch: `document.readyState === 'complete'` and an idle `jQuery.active`. Prefer it over
`waitForTimeout`.

**Do not add Angular's testability hook to it.** `window.getAllAngularTestabilities` is there, but
Angular 5's `isStable()` includes `!hasPendingMacrotasks`, and zone.js treats a repeating timer or an
animation-frame loop as a macrotask that never completes — so Composition, Deployment and Catalog are
never "stable" and the predicate becomes an unconditional hang (five tests timed out this way in
CI run 31798342250). Same reason Protractor needed `waitForAngularEnabled(false)`; the product's
`ProgressService` dodges it from the other side with `ngZone.runOutsideAngular()`.

It returns **`false` rather than throwing** when the page is frozen: a `page.evaluate()` that
never resolves means the Angular zone is blocked, which is the classic hang signature, and it must
fail loudly instead of timing out opaquely.

**Do not treat it as a wait for the page you navigated to.** It cannot be one. AngularJS is gone
(`4a302206e`), so `readyState === 'complete'` and an idle jQuery are both already true the moment an
in-page hash assignment returns. Measured on the 2026-08-14 CI run: 96 invocations across the suite,
every single one satisfied on its **first** evaluation, and not one 500 ms back-off anywhere in the
run. Anything measured straight afterwards is reading the *previous* tab, or an empty shell.

That is why:

- `gotoWorkspaceTab()` waits for `SEL.workspaceRoutedTab` — the element the workspace outlet
  activates — before returning, and `workspace-navigation.spec.ts` pins that contract with a bare
  `count()` (a web-first matcher there would retry and pass whether or not the helper waited);
- every assertion after a navigation either polls or uses a web-first matcher.

**A polled value must distinguish "absent" from "expected".** `workspace-gutter.spec.ts` polled
`routedPaddingLeft` to `0` while `measure()` reported a missing element as `0` — so the assertion
passed with no tab mounted at all, for both canvas tabs, 68 ms after the hash change. Report absence
as `null`, or anchor the poll on the element's presence first.

**Prefer identity over tag names in hit tests.** `elementFromPoint` returns the innermost box, and in
this app that is a `<div>` almost everywhere — `.workspace-tab-title` is a div, and every routed
tab's template roots at one. Comparing the hit *tag* against the routed component's tag therefore
never fails; ask whether the element you expect **is** the hit node or contains it
(`workspace-tab-title.spec.ts`'s `titleIsTopmost`, `workspace-topbar-controls.spec.ts`'s `hitSelf`).

---

## Navigation contract

**Navigate by URL. Never through `$state`.** Phase 13 of the AngularJS removal deleted
`angular-ui-router` (CR 2) and then the `angular` package itself (CR 3). Any spec reaching
`window.angular.…injector().get('$state').go(...)` broke at CR 2 and cannot even be *repaired*
at CR 3. The URL shape, by contrast, is pinned by `PathUtilities.java:256` ("the component id is
the segment immediately after `workspace`") and by ~70 Cypress URL lines, so it survives both.
Navigating this way also exercises deep-linking, which `$state.go()` never did.

Use `gotoWorkspaceTab(page, {id, type, tab})` and `gotoTopLevel(page, '/catalog')`.

### CREATE mode omits the id segment entirely

```
VIEW/EDIT: #!/dashboard/workspace/<id>/service/general
CREATE:    #!/dashboard/workspace/service/general       ← no id segment
```

ui-router matched the create form as `workspace//service/general` — a **double** slash, because
`:id` was simply absent from `$state.href`'s interpolation. The Angular Router cannot represent
that URL at all: `DefaultUrlSerializer.parse()` drops an empty segment *and everything after it*,
so the tab would be silently lost. `app.routes.ts` therefore declares
`':previousState/workspace/:type'` as a route of its own, ahead of the id-bearing one — the two
consume a different segment count, so order alone disambiguates them, and with `:id` absent
`WorkspaceComponentResolver` takes its create-an-empty-component branch. The single-slash form is
also what `cypress/integration/service-distribution.spec.js` always used, so that spec was right
about the shape and wrong only about the era.

### Assert views with `toBeAttached()`, not `toBeVisible()`

`<home-page>` and `<catalog-page>` are bare custom elements with no CSS rule of their own, so
they compute to `display: inline` and measure 0×0 **even when their content fills the viewport**
(the child `.sdc-catalog-container` is 1920 px wide but also 0-height, its children being
floated). `toBeVisible()` therefore reports "hidden" on a perfectly rendered page.

The pattern that works, and which each part of earns its place:

```ts
await expect(page.locator(SEL.catalogView)).toBeAttached();      // the outlet swapped the component
await expect(page.locator(SEL.homeView)).toHaveCount(0);         // ...and destroyed the old one
await expect(page.locator(SEL.catalogViewMarker).first()).toBeVisible();  // pixels were painted
```

The middle assertion is the one that catches the 2026-07-09 dead-click regression, where the URL
and the menu highlight both updated correctly while the view never changed — a URL-only
assertion passes and proves nothing.

### Cold deep-links work — and that is a fix worth knowing the history of

`page.goto('/sdc1#!/catalog')` lands on `/catalog`, and F5 on a workspace tab keeps your place.
Neither was true before phase 13 CR 2: every route but `/dashboard` silently fell back to the
dashboard, because Angular's `HashLocationStrategy.prepareExternalUrl()` is
`'#' + joinWithSlash(baseHref, internal)` and `internal` was `/!/catalog` — `path()` strips only the
leading `#`, so the `!` was treated as a path segment, the URL became `#/!/catalog`, the `#!` prefix
was destroyed and ui-router fell through to `otherwise('dashboard')`. Observed hashchange sequence:
`/!/catalog` → `!` → `!/dashboard`.

`SdcHashLocationStrategy` (`catalog-ui/src/app/ng2/utils/`) is the fix: it strips a leading `!` in
`path()` and re-inserts it in `prepareExternalUrl()`, so the `#!` prefix every bookmark, Cypress
spec and Selenium URL encodes survives. `gotoCold()` is now a single `page.goto()` — it used to be a
two-step workaround, and the day it can go back to being one is the day this regressed.

The reload test (*"page reload keeps you on the same workspace tab"*) originally asserted the
**broken** behaviour on purpose, so that CR 2 could not ship a still-broken deep link unnoticed. The
fix turned it red, and it was inverted rather than deleted; it now pins the working behaviour.

### Some tab URLs need a TRAILING SLASH

`composition/`, `deployment/` and `activity_log/` are declared with a trailing slash in
`app.routes.ts`, inherited verbatim from their ui-router `url:`. To the Angular Router that slash is
a real (empty) path segment, so `composition` does **not** match `/…/composition/` — omit it and the
URL falls through to the `'**'` wildcard and lands on `/dashboard`, so a spec that forgets it
measures the dashboard while believing it is on the canvas. `gotoWorkspaceTab(page, {tab:
'composition/'})` passes the segment through verbatim; keep the slash.

---

## Two different unsaved-changes modals

There are **two** dialogs spelled `navigate-modal`, with incompatible markup. Reaching for the
wrong one produces "no modal found" on a page that plainly has one — which reads as a broken guard
and is not.

| | Properties Assignment / Attributes-Outputs | General tab (and any tab with no modal of its own) |
| --- | --- | --- |
| Opened by | `ModalServiceSdcUI.openCustomModal` | `openWarningModal` in `UnsavedChangesFlagGuard.prompt()` |
| Dialog selector | `[data-tests-id="navigate-modal"]` | `div.sdc-modal` — **carries no `data-tests-id` at all** |
| Confirm button | `[data-tests-id="discardButton"]` | `[data-tests-id="navigate-modal-button-ok"]` |

The second row's button id is the trap: `openWarningModal`'s third argument (`'navigate-modal'`)
becomes the button-id **prefix**, *not* a testId on the dialog. So despite the button declaring
`testId: 'OK'`, no element ever carries `data-tests-id="OK"` here — measured live. Both are in
`SEL` (`navigateModal`/`discardButton` and `warningModal`/`warningModalOkButton`).

## Composition's layout comes from a class on `<body>`

`bodyClass: 'composition'` lives in the route's `data` (`app.routes.ts`); `RouteMetadataService`
writes it onto `<body>` on every `NavigationEnd` — swapping only the class it previously applied, so
that `modal-open` and any other foreign body class survives. `workspace.less` then keys the
full-bleed geometry off `.composition … router-outlet ~ *`. Nothing in Jest, AOT or Selenium models
that chain. Measured at 1920×1080:

| | `<body>` class | routed child tab | shell sidebar |
| --- | --- | --- | --- |
| General | `general` | `242,103` 1678×977 | present, 242 px |
| Composition | `composition` | `0,50` 1920×1030 | **absent** (`*ngIf="!isComposition"`) |

The routed child tab is the component the shell's `<router-outlet>` activated, matched as
`router-outlet ~ *` — it is a *sibling* of the outlet, and no wrapper element may be introduced,
because `.w-sdc-main-right-container`'s class attribute must stay exactly that string for the
Selenium exact-class xpath waits.

`composition-geometry.spec.ts` measures **both** tabs in one test on purpose: the absolute numbers
are viewport-dependent, so what pins the behaviour is the *difference*. It also hit-tests
`elementFromPoint(300, 400)` for `CANVAS`, because geometry alone passes on a canvas covered by a
click-swallowing overlay.

---

## Writing new tests

1. Import from `./fixtures/sdc`, take `{ sdcPage, api }`.
2. Create data through `api`, not the UI, unless creating it *is* the thing under test.
3. Navigate with `gotoWorkspaceTab` / `gotoTopLevel`.
4. **Wait for the thing you are about to assert on**, never `waitForTimeout`. `settles(sdcPage)` is
   not that wait — see *Settle detection* above; `gotoWorkspaceTab` already waits for the tab to
   mount, and beyond that use a web-first matcher, `expect.poll`, or `waitForResponse` on the
   request whose result you are reading.
5. Add selectors to `SEL` rather than inlining them.
6. **Assert on the effect, not the appearance.** The data-loss bug left the field showing the
   typed text while sending nothing, so `general-tab-save.spec.ts` asserts on the PUT *body*.
   `page.waitForRequest` and `page.route` are the tools that make a spec worth having.
7. **Watch your new spec fail.** Break the thing it guards, confirm red, restore. A spec never
   seen red is not known to pin anything — the top-nav guard here was validated by reintroducing
   the real regression in `navigation.service.ts` and watching it go red.

Note the trap in `api.deleteAsset()`: deleting a **wrong** id also returns 204, so a 204 is not
evidence the intended asset is gone.

---

## CI

This suite runs in **GitHub Actions**, not Jenkins: `.github/workflows/gerrit-verify-playwright.yaml`.
ONAP is migrating CI off Jenkins/`ci-management` onto GHA, so new jobs are added there.

**It is VOTING.** The `vote` job casts `Verified+1` on success and `Verified-1` on failure, so a red
run blocks submission and a green Gerrit does mean this suite passed. `notify` clears the job's own
previous vote when a run starts, so an in-flight run never shows a stale `+1`. To take it back off
the gate, set `comment-only: true` on both steps — the run then only comments.

Two behaviours of the gate worth knowing:

- **A flaky `-1` is cleared with `recheck`,** exactly as for the Jenkins CSIT jobs: commenting
  `recheck` on the change re-dispatches this workflow. The suite is not flake-free — 146984 passed
  and then failed on a `recheck` of the same patchset, and `checkout-gerrit-change-action` does a
  bare `git checkout FETCH_HEAD`, so both runs built an identical tree.
- **A cancelled run votes `Verified-1` *and* `Code-Review-1`** on the patchset it was running
  against. That is `gerrit-review-action`'s contract for `cancelled`, not a real failure, and
  pushing a new patchset triggers it via `cancel-in-progress`. The vote lands on the superseded
  patchset; the new one gets its own run.

The workflow is a self-contained three-job pipeline (`notify` → `playwright` → `vote`) rather than
a thin caller of `lfit/releng-reusable-workflows/.../compose-maven-verify.yaml`, because that
reusable workflow uploads no artifacts — and for a UI suite the traces *are* the debugging story.
It runs two Maven commands, the same two documented above:

```bash
mvn clean install -P all,docker -DskipTests ...          # build images
mvn verify -P run-integration-tests-playwright -f integration-tests/pom.xml
```

Every run uploads a `playwright-report` artifact — the HTML report, with every screenshot and
trace embedded in it, plus the JUnit XML — retained 14 days, with `if: always()` so a failing run
still produces it. Unzip it and serve it:

```bash
unzip -q playwright-report.zip -d /tmp/pw-report
npx playwright show-report /tmp/pw-report/playwright-report --host 0.0.0.0 --port 9323
```

Traces open in the report's own bundled viewer, so you need neither `show-trace` nor
trace.playwright.dev. Opening `index.html` over `file://` will not load them.

`--host`/`--port` are what make this work on a headless box, and are not optional there. Without
them both viewers try to open a browser window of their own and die with

```
Error: Protocol error (Browser.getVersion): Internal server error, session closed.
```

which says nothing about the real cause. With them, the viewer is served over HTTP and you tunnel
to it instead. `show-trace` takes the same two flags.

Also note `show-trace` is the wrong command for this artifact regardless: it is a *report* bundle —
an `index.html` plus one trace zip per test under `playwright-report/data/`, keyed by content SHA-1
— not a trace. Use `show-report` on the extracted directory, or point `show-trace` at one member of
`data/`.

The workflow deliberately does **not** upload the raw `target/playwright-results/`, and re-adding
it is the one edit to avoid. The html reporter *copies* every attachment into
`playwright-report/data/` keyed by content SHA-1, so shipping the output dir as well sent a second
byte-identical copy of all 32 traces — 145 MB where 75 MB holds the same bytes (run #27; verified
by hashing all 36 files). `trace: 'on'` stays as it is: one trace per test is the point, and it is
the duplication that was wasteful, not the traces.

On failure it additionally dumps `docker ps -a` plus the last 200 log lines of every container,
because a stack that never became healthy is the most common failure mode and is invisible in the
Playwright report.

Two constraints worth knowing before editing that workflow:

- **JDK 11, not 17.** SDC is Java 11 source/target. The LF reusable workflows default to 17.
- **The disk-reclaim step is load-bearing.** A GitHub-hosted runner has ~14 GB free; the stack
  needs ~6 GB of images plus the build tree. Removing the preinstalled Android/.NET/GHC toolchains
  buys ~30 GB. Without it the job dies mid-build with an ENOSPC that reads like a build error.
  Jenkins ran this on an 8c-8g node that needed no such help.

The Jenkins CSIT jobs in `ci-management/jjb/sdc/sdc-csit.yaml` are unchanged and still run; this
job is additive, and replaces neither:

| subproject | Maven profile | What runs |
| --- | --- | --- |
| `ui` | `docker,run-integration-tests-ui` | TestNG + Selenium (`onapUiSanity.xml`, `helmValidatorTests.xml`) |
| `api` | `docker,run-integration-tests-api` | TestNG backend API suite |

> **Gerrit trigger.** The workflow declares `workflow_dispatch` with the standard `GERRIT_*` input
> block, which is what `gerrit_to_platform` dispatches into — the same mechanism `gerrit-clm.yaml`
> uses. Wiring the per-patchset trigger itself is LF releng-side configuration, not something this
> repo controls; until it is enabled, run the job manually from the Actions tab.

### Editing the workflow needs its own change

A change that touches `.github/**` may touch **nothing else**. ONAP's org-level required pipeline
(`onap/.github` → `gerrit-required-verify.yaml`) runs
`lfit/releng-reusable-workflows/.../gerrit-compose-required-change-isolation-verify.yaml`, which
fails the `ONAP Required GHA` Verified vote when in-scope and out-of-scope files are combined in one
commit. The check is per-commit (`base-ref: HEAD~1`), so stacking two changes in a relation chain
satisfies it while a single squashed commit does not — and note this cuts both ways: a change to
this file cannot carry a `.github/` edit along with it either. That is why this section and the
workflow it describes arrived as two separate changes.

### Never regenerate the lockfile behind a corporate proxy

`package-lock.json` is committed, and every `"resolved"` URL in it must point at
`https://registry.npmjs.org/`. This is the one way a purely local action breaks CI:

npm rewrites `"resolved"` to whatever registry it installed *from*, so running `npm install` behind
a private mirror (`.npmrc` with `registry=https://artifactory…/api/npm/registry.npmjs.org/`, as a
Deutsche Telekom workstation has) silently rewrites all of them to that host. npm then honours the
`"resolved"` URLs over *any* registry setting — a `--registry` flag and a project `.npmrc` both
leave them untouched — so CI, which holds no credentials for that mirror, fails every fetch with:

```
npm error code E401
npm error Incorrect or missing password.
```

The pom therefore runs **`npm ci`**, which fails on lockfile drift rather than rewriting it, and a
project `.npmrc` pins `registry=https://registry.npmjs.org/` so a regeneration on a
proxy-configured machine stays public. Check before committing a lockfile change:

```bash
grep -o '"resolved": "[^"]*"' package-lock.json | grep -v registry.npmjs.org   # must print nothing
```

To repair a lockfile that already has private URLs, rewrite the host in place — that keeps the
pinned versions and their `integrity` hashes, which a full regeneration would churn:

```bash
sed -i 's#https://artifactory\.devops\.telekom\.de/artifactory/api/npm/registry\.npmjs\.org/#https://registry.npmjs.org/#g' package-lock.json
```

Do **not** point this at ONAP's own `npm.public` mirror instead: it resolves, but rewrites the URLs
to plain `http://nexus3.onap.org`, trading one non-portable lockfile for another.

### The Selenium grid is skipped here

`integration-tests/pom.xml` gates the `selenium/standalone-firefox` container on a new
`it.selenium.disabled` property, set to `true` **only** in the `run-integration-tests-playwright`
profile. Playwright drives its own bundled Chromium, so that 1.45 GB image was pure dead weight —
and on a disk-constrained runner, skipping it is the difference between fitting and not.

The property defaults to `false`, so the two Jenkins profiles above are byte-identical to before.
Verify with:

```bash
mvn help:evaluate -Dexpression=it.selenium.disabled -q -DforceStdout \
  -f integration-tests/pom.xml -P run-integration-tests-ui    # -> false
```
