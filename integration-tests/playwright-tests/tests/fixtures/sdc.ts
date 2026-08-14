/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

/**
 * Shared fixtures and helpers for the SDC Playwright suite.
 *
 * Import `test` from HERE, not from '@playwright/test' — this module re-exports a `test`
 * extended with an auto-login `sdcPage` fixture and an `api` fixture for asset creation.
 *
 *     import { test, expect, SEL } from './fixtures/sdc';
 *     test('...', async ({ sdcPage, api }) => { ... });
 *
 * DESIGN CONSTRAINT — why navigation goes through the URL, not through $state:
 *   Phase 13 of the AngularJS removal deletes `angular-ui-router` (CR 2) and then the
 *   `angular` package itself (CR 3). Any spec that reaches `window.angular.…injector()
 *   .get('$state').go(...)` therefore stops working at CR 2 and cannot even be repaired
 *   at CR 3, because there is no AngularJS injector left to reach.
 *
 *   `gotoWorkspaceTab()` below navigates by writing the hash URL instead. That URL shape
 *   is a hard contract that survives BOTH CRs: the '#!' prefix is preserved by
 *   SdcHashLocationStrategy and the '/<previousState>/workspace/<id>/<type>/<tab>' shape
 *   is pinned by Selenium (PathUtilities.java) and 70 Cypress URL lines. Navigating by URL
 *   is thus the only framework-agnostic option, AND it additionally exercises deep-linking,
 *   which $state.go() never did.
 *
 * See README.md "Navigation contract" for the full rationale.
 */

import { test as base, expect, Page, APIRequestContext, APIResponse } from '@playwright/test';

// ---------------------------------------------------------------------------
// Environment
// ---------------------------------------------------------------------------

/** webseal-simulator password for every preconfigured user (openecomp-be/tools/webseal-simulator). */
export const SIM_PASSWORD = '123123a';

/** cs0008 = Carlos Santana, role DESIGNER. The role every workspace test needs. */
export const DESIGNER_USER = 'cs0008';

/**
 * True when SDC_BASE_URL points at the webpack dev server rather than the webseal-simulator
 * (:8285). The dev server injects its own auth cookies and serves a built-in /login page, but it
 * has no plugins-config endpoint, so it raises a transient "Not Found" modal that must be
 * dismissed before clicks land.
 *
 * Matches :9000-:9009, not just :9000, because `webpack.server.js` honours SDC_DEV_PORT: verifying
 * a bootstrap change means running a baseline and a patched dev server side by side, and a
 * hardcoded :9000 silently skipped the plugins stub on every other port — which surfaces as a dozen
 * unrelated click timeouts rather than as a configuration error. The range stops at :9009 on
 * purpose; :9042 is Cassandra's port in the Docker stack.
 */
export const IS_DEV_SERVER = /:900\d(\/|$)/.test(process.env.SDC_BASE_URL || '');

// ---------------------------------------------------------------------------
// Selectors — single source of truth
//
// Every selector here is ALSO depended on by Selenium page objects and/or Cypress specs,
// so they are contracts, not conveniences. Changing one breaks three test suites.
// ---------------------------------------------------------------------------

export const SEL = {
    /** Top-nav HOME button. `top-nav.component.html:30` builds it from the menu item text. */
    homeButton: '[data-tests-id="main-menu-button-home"]',
    catalogButton: '[data-tests-id="main-menu-button-catalog"]',

    /**
     * The rendered VIEW for each top-level route: the component the app-level <router-outlet>
     * activates. These are the discriminators for the 2026-07-09 dead-click regression, where the
     * URL and the menu highlight both updated correctly while the view never changed — so a
     * URL-only assertion passes and proves nothing. The outlet swaps the two, so exactly one is
     * ever attached.
     *
     * ASSERT WITH toBeAttached(), NOT toBeVisible(). Both <home-page> and <catalog> are bare
     * custom elements with no CSS rule of their own, so they compute to `display: inline` and
     * measure 0x0 even when their content fills the viewport (their child .sdc-catalog-container
     * is 1920px wide but also 0-height, as its own children are floated). toBeVisible() therefore
     * reports "hidden" on a perfectly rendered page. Use the *Marker selectors below when a test
     * needs to prove pixels were actually painted.
     *
     * NOTE THE ASYMMETRY, it is not a typo: the catalog element is <catalog>, not <catalog-page>.
     * Under ui-router both views were reached through AngularJS `downgradeComponent` wrappers named
     * after the states' templates (directive-module.ts registered 'catalogPage' and 'homePage'), and
     * CatalogComponent's own selector is 'catalog' while HomeComponent's really is 'home-page'. With
     * the wrappers deleted in phase 13 CR 2 the router activates each component directly, so the
     * element is now the component's own selector and only the catalog one changed name.
     */
    homeView: 'home-page',
    catalogView: 'catalog',

    /**
     * Genuinely-painted content inside each view — verified live at >40x10 px. Pair these with the
     * *View selectors: the view element proves the outlet swapped the component, the marker proves
     * the template actually rendered rather than erroring out to an empty shell.
     */
    homeViewMarker: '[data-tests-id="dashboard-Elements"]',
    catalogViewMarker: '[data-tests-id="statusFilterTitle"]',

    /**
     * The top-nav's HOME entry exists ONLY outside the workspace. Inside a workspace the same slot
     * is re-rendered as a breadcrumb trail (top-nav.component.html:39), so HOME becomes
     * breadcrumbs-button-0 and `main-menu-button-home` is absent from the DOM entirely. Using the
     * wrong one inside the workspace produces a 60 s click timeout, not a helpful failure.
     */
    homeBreadcrumb: '[data-tests-id="breadcrumbs-button-0"]',

    /** Workspace left sidebar — General tab entry. `workspace-container.component.html:18`. */
    generalSideMenu: '[data-tests-id="GeneralLeftSideMenu"]',
    /** Any sidebar tab button, by its visible label. */
    sideMenu: (label: string) => `[data-tests-id="${label}LeftSideMenu"]`,
    /** All sidebar items — used to assert the sidebar is populated at all. */
    sideMenuItems: '.i-sdc-designer-sidebar-section-content-item',

    /**
     * The loader Selenium's LoaderHelper.waitForLoader() waits on. THREE elements can carry
     * data-tests-id="loader": <sdc-loader testId="loader"> (app.component.html:17) and the two
     * inline divs in workspace-container.component.html (:5 and :46). All three must keep it.
     */
    loader: '[data-tests-id="loader"], .tlv-loader, .sdc-loader-global-wrapper.sdc-loader-background',

    /** General-tab form fields. */
    name: '[data-tests-id="name"]',
    description: '[data-tests-id="description"]',
    category: '[data-tests-id="selectGeneralCategory"]',
    vendorName: '[data-tests-id="vendorName"]',
    vendorRelease: '[data-tests-id="vendorRelease"]',
    versionHeader: '[data-tests-id="versionHeader"]',

    /**
     * The CREATE button lives in the workspace CHROME (top bar); the General tab's SAVE button
     * lives in the form's action-buttons row. Both carry data-tests-id="create/save", so an
     * unscoped selector is ambiguous — always scope by the wrapper class.
     */
    chromeCreateButton: '.sdc-workspace-top-bar-buttons [data-tests-id="create/save"]',
    formSaveButton: '.w-sdc-main-container-body-content-action-buttons [data-tests-id="create/save"]',

    /**
     * EXACT-class xpath contract: 8 Selenium page objects build their locator as
     * "//div[@class='%s']" with NO contains() — 7 for 'w-sdc-main-right-container' (e.g.
     * pages/AttributesPage.java, pages/home/HomePage.java) and ResourceWorkspaceTopBarComponent
     * for 'sdc-workspace-top-bar'. Adding ANY class to either element breaks every one of them.
     * Asserted by workspace-shell.spec.ts.
     */
    mainRightContainer: 'div.w-sdc-main-right-container',
    topBar: 'div.sdc-workspace-top-bar',

    /**
     * The element the workspace <router-outlet> activates for the current tab, whatever its tag
     * (`general-tab`, `deployment-page`, or a bare `ng-component` for the two selectorless pages).
     * Matched as the outlet's element SIBLING because the outlet itself renders nothing — Angular
     * inserts the activated component after the <router-outlet> comment anchor, not inside it. There
     * is deliberately no wrapper element to match instead: `.w-sdc-main-right-container`'s class
     * attribute has to stay exactly that string for the Selenium exact-class xpath waits. Scoped to
     * the shell so the app-level outlet in app.component.html cannot match.
     *
     * This is what "the tab has mounted" means, and it is the only tab-agnostic way to say it, so
     * gotoWorkspaceTab() waits on it and three layout specs measure it.
     */
    workspaceRoutedTab:
        '.sdc-workspace-container .w-sdc-main-right-container > .w-sdc-main-right-container-content > router-outlet ~ *',

    /**
     * The PA/AO unsaved-changes modal, opened via ModalServiceSdcUI.openCustomModal with
     * testId: "navigate-modal" (properties-assignment.page.component.ts:1301,
     * attributes-outputs.page.component.ts:696). Its three buttons are a Cypress contract.
     */
    navigateModal: '[data-tests-id="navigate-modal"]',
    discardButton: '[data-tests-id="discardButton"]',

    /**
     * The SECOND, different unsaved-changes modal: UnsavedChangesFlagGuard's `prompt()`, opened with
     * openWarningModal(). It guards the General tab, which has no modal of its own. (Before phase 13
     * CR 2 the same modal was raised by app.ts:619-638's `onNavigateOut`.)
     *
     * Its markup does NOT match the custom-modal one, which is why a `.custom-modal` or
     * `[data-tests-id="navigate-modal"]` selector finds nothing here — measured live:
     *   - the dialog is `div.sdc-modal` (legacy onap-ui-angular markup) and carries NO
     *     data-tests-id at all, so it can only be reached by class;
     *   - openWarningModal's third argument ('navigate-modal') is NOT emitted as a testId on the
     *     dialog. It becomes the button-id PREFIX, so the OK button's real attribute is
     *     data-tests-id="navigate-modal-button-ok" — NOT the bare 'OK' that the button's own
     *     `testId: 'OK'` suggests and that the phase 13 CR 2 plan records.
     */
    warningModal: 'div.sdc-modal',
    warningModalOkButton: '[data-tests-id="navigate-modal-button-ok"]',
    okButton: "[data-tests-id='OK']",
} as const;

// ---------------------------------------------------------------------------
// Settle detection
// ---------------------------------------------------------------------------

/**
 * Derived from AdditionalConditions.pageLoadWait() in the Selenium suite, which is what CI gates on.
 *
 * The `window.angular` branch that predicate carries is GONE here, not skipped: AngularJS was
 * removed in 4a302206e, so the digest/$http/$timeout checks can never run and keeping them implied a
 * synchronisation this helper does not provide. What replaces them is Angular's own stability signal
 * — the registry `platform-browser`'s BrowserGetTestability installs on window, the same one
 * Protractor's waitForAngular() gated on.
 *
 * That registry is consulted BEST-EFFORT on purpose: an empty one means "no hook on this page", not
 * "stable", and collapsing those two is precisely the mistake that made this predicate vacuous. So
 * an absent or empty registry falls through to the readyState/jQuery pair rather than blocking, and
 * the load-bearing waits live at the call sites (see the note on settles() below).
 */
const PAGE_LOAD_WAIT = `
  try {
    if (document.readyState !== 'complete') return false;
    if (window.jQuery && window.jQuery.active) return false;
    var getTestabilities = window.getAllAngularTestabilities;
    if (typeof getTestabilities === 'function') {
      var testabilities = getTestabilities();
      for (var i = 0; i < testabilities.length; i++) {
        if (!testabilities[i].isStable()) return false;
      }
    }
    return true;
  } catch (ex) { return false; }
`;

/**
 * Waits until the page is quiescent by the predicate above.
 *
 * NOT a substitute for waiting on the thing under test, and it cannot be made into one. With
 * AngularJS gone, readyState is already 'complete' and jQuery already idle the instant an in-page
 * hash assignment returns, so on a route change this can be satisfied before the router has begun
 * resolving — a one-shot measurement taken straight afterwards is reading the PREVIOUS tab, or no
 * tab at all. That is why gotoWorkspaceTab() waits for the routed tab itself and why every
 * assertion downstream polls or uses a web-first matcher. Treat this as a cheap extra barrier.
 *
 * Returns false on timeout AND on a frozen page — a page.evaluate() that never resolves means the
 * Angular zone is blocked, which is the classic hang signature and must fail loudly rather than
 * time out silently.
 */
export async function settles(page: Page, timeoutMs = 30_000): Promise<boolean> {
    const start = Date.now();
    while (Date.now() - start < timeoutMs) {
        const r = await page.evaluate(new Function(PAGE_LOAD_WAIT) as any).catch(() => 'FROZEN');
        if (r === 'FROZEN') return false;
        if (r === true) return true;
        await page.waitForTimeout(500);
    }
    return false;
}

/**
 * Dismisses the transient "Not Found" error modal if one is open. Kept for specs that trigger a
 * modal mid-flow; the dev-server plugins-config case is prevented at source by stubPluginsConfig().
 */
export async function dismissTransientModal(page: Page): Promise<void> {
    const okBtn = page.locator(`${SEL.okButton}, button:has-text('OK')`);
    if (await okBtn.count()) {
        await okBtn.first().click({ timeout: 3_000 }).catch(() => { /* modal closed itself */ });
        await page.waitForTimeout(500);
    }
}

/**
 * The dev server proxies '/sdc1/feProxy/rest/*' but NOT '/sdc1/rest/config/ui/plugins', which the
 * catalog-fe servlet serves in the Docker stack. The resulting 404 raises an error modal whose
 * `.modal-background` backdrop then intercepts every click, and — because the request is fired
 * lazily, after login — dismissing the modal once at login is too early: it reappears mid-spec.
 *
 * So stub the endpoint instead of racing its modal. An empty plugin list is the honest stub: none
 * of the plugins the FE returns (POLICY, WORKFLOW, …) is reachable from a dev server anyway.
 *
 * DEV SERVER ONLY, deliberately. Plugin entries become workspace sidebar tabs, so stubbing this
 * against the Docker stack would change what the sidebar specs see — and that stack is the
 * CI-faithful path, which must stay byte-identical to what Selenium exercises. The divergence is
 * therefore in the direction of the *less* authoritative target, and it is bounded to plugin tabs,
 * which no spec here asserts on.
 */
export async function stubPluginsConfig(page: Page): Promise<void> {
    await page.route('**/sdc1/rest/config/ui/plugins', (route) =>
        route.fulfill({ status: 200, contentType: 'application/json', body: '[]' }));
}

// ---------------------------------------------------------------------------
// Login
// ---------------------------------------------------------------------------

/**
 * Logs in through the webseal-simulator (or the dev server's equivalent /login page) and
 * waits for the SDC shell to render. Asserting on the HOME button rather than a fixed sleep
 * is what makes this reliable; the previous per-spec helpers used waitForTimeout(5000).
 */
export async function login(page: Page, userId: string = DESIGNER_USER): Promise<void> {
    await page.goto('/login');
    await page.locator('input[name="userId"]').fill(userId);
    await page.locator('input[name="password"]').fill(SIM_PASSWORD);
    await page.locator('input[value="Login"]').click();
    await page.waitForURL('**/sdc1**', { timeout: 30_000 });
    await expect(page.locator(SEL.homeButton)).toBeVisible({ timeout: 30_000 });
}

// ---------------------------------------------------------------------------
// URL-based navigation — the CR2/CR3-proof path
// ---------------------------------------------------------------------------

/**
 * Loads a deep '#!' route from cold — a real browser navigation, so the URL is parsed BEFORE the
 * app bootstraps, which is the part no in-page hash assignment reaches.
 *
 * This used to be a two-step workaround (load '#!/dashboard', then assign the hash), because a
 * plain goto() to any route but '/dashboard' silently landed on the dashboard: Angular's
 * HashLocationStrategy.prepareExternalUrl() is `'#' + joinWithSlash(baseHref, internal)` and
 * path() strips only the leading '#', so '#!/catalog' became '#/!/catalog' and destroyed
 * ui-router's '#!' prefix, which then fell through to otherwise('dashboard'). SdcHashLocationStrategy
 * (phase 13 CR 2) re-inserts the '!' on write and tolerates it on read, so the single goto() below
 * is now the whole story — keep it that way; a regression here shows up as a cold load landing on
 * the dashboard, which is what the deep-link test asserts against.
 */
export async function gotoCold(page: Page, path: string): Promise<void> {
    await page.goto(`/sdc1#!${path}`);
    await settles(page);
}

/**
 * Navigates to a workspace child tab by URL.
 *
 * The '#!' hash prefix and the '/<previousState>/workspace/<id>/<type>/<tab>' segment order
 * are contracts pinned by Selenium's PathUtilities.java:256 ("the component id is the URL
 * segment immediately after 'workspace'") and by 70 Cypress URL lines. They therefore survive
 * the ui-router removal, which is exactly why navigating this way is durable.
 *
 * Uses a HASH ASSIGNMENT rather than page.goto() by default: an in-page hash change is an order
 * of magnitude faster than a full reload and is what a user clicking through the app produces.
 * Pass `cold: true` for a real browser navigation, which additionally exercises the pre-bootstrap
 * URL parse — a distinct behaviour worth its own assertions.
 *
 * RETURNS ONLY ONCE THE TARGET TAB HAS MOUNTED. This is the helper's contract and it is load-bearing:
 * settles() cannot provide it (see its note), so without an explicit wait here every caller that
 * measured the DOM in one shot was reading the tab it navigated AWAY from — or an empty shell. Pass
 * `expectTab: false` for a URL that deliberately activates no workspace tab.
 */
export async function gotoWorkspaceTab(
    page: Page,
    opts: {
        id?: string; type: 'resource' | 'service'; tab?: string; previousState?: string;
        cold?: boolean; expectTab?: boolean;
    },
): Promise<void> {
    const {
        id, type, tab = 'general', previousState = 'dashboard', cold = false, expectTab = true,
    } = opts;
    // CREATE mode is a SINGLE slash: '#!/dashboard/workspace/service/general'. ui-router matched
    // the DOUBLE-slash form ('workspace//service/general') because ':id' was simply absent from
    // $state.href's interpolation, but the Angular Router cannot represent that URL at all —
    // DefaultUrlSerializer.parse() drops an empty segment AND everything after it, so the tab
    // would be lost. app.routes.ts therefore declares ':previousState/workspace/:type' as its own
    // route, ahead of the id-bearing one, and this is the form that matches it. Incidentally it is
    // also the shape cypress/integration/service-distribution.spec.js always used.
    const path = id
        ? `/${previousState}/workspace/${id}/${type}/${tab}`
        : `/${previousState}/workspace/${type}/${tab}`;

    if (cold) {
        await gotoCold(page, path);
    } else {
        await page.evaluate((p) => { window.location.hash = `#!${p}`; }, path);
    }
    await settles(page);
    if (expectTab) {
        await expect(page.locator(SEL.workspaceRoutedTab).first(),
            `no workspace tab mounted at '${path}' — the route never resolved`)
            .toBeAttached({ timeout: 30_000 });
    }
}

/** Navigates to a top-level route (dashboard, catalog, onboardVendor, adminDashboard, …). */
export async function gotoTopLevel(page: Page, path: string): Promise<void> {
    await page.evaluate((p) => { window.location.hash = `#!${p}`; }, path);
    await settles(page);
}

/** Reads the current logical route from the hash — framework-agnostic, works before and after CR 2. */
export async function currentRoute(page: Page): Promise<string> {
    return page.evaluate(() => window.location.hash.replace(/^#!?/, ''));
}

// ---------------------------------------------------------------------------
// Asset creation via the REST API
// ---------------------------------------------------------------------------

export interface CreatedAsset {
    id: string;
    name: string;
    type: 'resource' | 'service';
}

/**
 * Creates assets through Playwright's APIRequestContext rather than through the page's
 * $http service. Two reasons this matters beyond avoiding `window.angular`:
 *   1. It survives CR 3 (no AngularJS injector to reach).
 *   2. It does not perturb the page under test — an in-page $http.post adds a pending request
 *      that pageLoadWait() then blocks on, which made the old specs racy.
 *
 * The USER_ID header is what the backend authorises on; the simulator's cookies are only
 * needed by the browser, not by direct API calls.
 */
export class SdcApi {
    constructor(private request: APIRequestContext, private baseUrl: string) {}

    private get headers() {
        return { 'USER_ID': DESIGNER_USER, 'Content-Type': 'application/json' };
    }

    private url(path: string): string {
        return `${this.baseUrl}/sdc1/feProxy/rest/v1${path}`;
    }

    private static collectionOf(type: 'resource' | 'service'): string {
        return type === 'resource' ? 'resources' : 'services';
    }

    /**
     * Turns a creation response into a CreatedAsset — but not before the backend can READ the asset
     * back.
     *
     * The read-back poll is not belt and braces. JanusGraph/Cassandra offers no read-after-write
     * guarantee across the BE's transactions, so a 201 from the create endpoint is sometimes followed
     * — within the same second — by a 404/SVC4063 on the very uniqueId it just handed out. The UI
     * then bounces to the dashboard behind an error modal and the spec fails on a missing sidebar,
     * which reads as a UI regression and is not one. `retries: 1` compounded it: the second attempt
     * passed, so the run reported all-green and the flake appeared only in the HTML report, never in
     * the JUnit XML the gate votes on.
     *
     * Polled on `filteredDataByParams?include=metadata` specifically because that is the request the
     * workspace fires on entry — the one that actually 404'd, not a proxy for it.
     */
    private async readable(
        resp: APIResponse, name: string, type: 'resource' | 'service',
    ): Promise<CreatedAsset> {
        const body = await resp.json().catch(() => ({}));
        if (resp.status() !== 201) {
            // Built ONLY on the failure path. `body` is ~4 KB of component JSON, and an eager
            // template literal put all of it into the HTML report's step title once per created
            // asset — the bulk of a 682 KB index.html across a run, and of every trace.
            expect(resp.status(), `${type} creation failed: ${JSON.stringify(body)}`).toBe(201);
        }
        const id: string = body.uniqueId;
        expect(id, `${type} creation returned 201 with no uniqueId`).toBeTruthy();

        const probe = this.url(
            `/catalog/${SdcApi.collectionOf(type)}/${id}/filteredDataByParams?include=metadata`);
        await expect.poll(
            async () => (await this.request.get(probe, { headers: this.headers })).status(),
            {
                timeout: 30_000,
                message: `${name} (${id}) was created but never became readable — entering its `
                    + 'workspace would 404 and bounce to the dashboard',
            }).toBe(200);

        return { id, name, type };
    }

    /** Creates a VF resource in NOT_CERTIFIED_CHECKOUT state (i.e. immediately editable). */
    async createVf(namePrefix = 'PwVF'): Promise<CreatedAsset> {
        const name = namePrefix + Date.now();
        const resp = await this.request.post(this.url('/catalog/resources'), {
            headers: this.headers,
            data: {
                name,
                description: 'created by the Playwright suite',
                componentType: 'RESOURCE',
                resourceType: 'VF',
                categories: [{
                    name: 'Generic',
                    normalizedName: 'generic',
                    uniqueId: 'resourceNewCategory.generic',
                    subcategories: [{
                        name: 'Abstract',
                        normalizedName: 'abstract',
                        uniqueId: 'resourceNewCategory.generic.abstract',
                    }],
                }],
                vendorName: 'pwVendor',
                vendorRelease: '1.0',
                contactId: DESIGNER_USER,
                icon: 'defaulticon',
                tags: [name],
            },
        });
        return this.readable(resp, name, 'resource');
    }

    /**
     * Creates a Service. Needed for the service-only General-tab fields (Service Role /
     * Service Function), which a VF never renders.
     */
    async createService(namePrefix = 'PwSvc'): Promise<CreatedAsset> {
        const name = namePrefix + Date.now();
        const resp = await this.request.post(this.url('/catalog/services'), {
            headers: this.headers,
            data: {
                name,
                description: 'created by the Playwright suite',
                componentType: 'SERVICE',
                categories: [{
                    name: 'Network Service',
                    normalizedName: 'network service',
                    uniqueId: 'serviceNewCategory.network service',
                }],
                contactId: DESIGNER_USER,
                icon: 'defaulticon',
                tags: [name],
            },
        });
        return this.readable(resp, name, 'service');
    }

    /**
     * Deletes an asset. Note the trap recorded in the regression-test skill: the wrong id ALSO
     * returns 204, so a 204 is not evidence the intended asset is gone.
     */
    async deleteAsset(asset: CreatedAsset): Promise<void> {
        const collection = SdcApi.collectionOf(asset.type);
        await this.request
            .delete(this.url(`/catalog/${collection}/${asset.id}`), { headers: this.headers })
            .catch(() => { /* best-effort cleanup; a leaked test asset must not fail the suite */ });
    }
}

// ---------------------------------------------------------------------------
// Fixtures
// ---------------------------------------------------------------------------

interface SdcFixtures {
    /** A page already logged in as cs0008 (DESIGNER) with the SDC shell rendered. */
    sdcPage: Page;
    /** REST client for creating/deleting assets out-of-band. */
    api: SdcApi;
}

export const test = base.extend<SdcFixtures>({
    sdcPage: async ({ page }, use) => {
        // Must be registered BEFORE login: the request fires during app bootstrap, and a route
        // added afterwards would miss it. See stubPluginsConfig() for why this is dev-server only.
        if (IS_DEV_SERVER) {
            await stubPluginsConfig(page);
        }
        await login(page);
        await use(page);
    },

    api: async ({ playwright, baseURL }, use) => {
        const ctx = await playwright.request.newContext({ ignoreHTTPSErrors: true });
        await use(new SdcApi(ctx, baseURL || 'http://localhost:8285'));
        await ctx.dispose();
    },
});

export { expect };
