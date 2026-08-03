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
 * Routing and navigation guards.
 *
 * WHAT THIS GUARDS — the behaviours the ui-router → Angular Router swap (phase 13 CR 2) puts
 * at risk, none of which any Jest route-config test can catch because they all need a real
 * browser history and a real render:
 *   1. Top-nav HOME/CATALOG buttons change the VIEW, not just the highlight. This is the exact
 *      2026-07-09 regression: the buttons were routed through the Angular Router while the view
 *      was still rendered by ui-router's <ui-view>, so they became dead clicks.
 *   2. Deep-linking / page reload on a workspace child URL. A cold load of a deep hash route is
 *      the single most fragile part of a router swap and is exercised NOWHERE else. It is also
 *      what SdcHashLocationStrategy exists for — it was broken product-side before CR 2.
 *   3. Browser BACK and FORWARD. Untested before; the Angular Router's history integration is
 *      completely different from ui-router's.
 *   4. An unknown route falls back to the dashboard (ui-router's `otherwise`, now the `'**'`
 *      wildcard route in app.routes.ts).
 *   5. The settle contract Selenium's AdditionalConditions.pageLoadWait() gates CI on.
 *
 * HOW TO RUN: see README.md — `npx playwright test workspace-navigation`.
 */

import {
    test, expect, SEL, settles, currentRoute, gotoTopLevel, gotoWorkspaceTab, dismissTransientModal,
} from './fixtures/sdc';

test.describe('Navigation and routing', () => {

    /**
     * Real clicks on the top-nav, asserting the VIEW changed and not merely the URL or the
     * highlight. This is the 2026-07-09 regression: routing HOME/CATALOG through the Angular
     * Router while their views were still rendered by ui-router's <ui-view> left the URL and the
     * menu highlight correct while the view never changed, so a URL-only assertion would pass.
     *
     * Three assertions per direction, because each catches a different failure:
     *   - the URL changed (the router ran at all);
     *   - the new view element is attached AND the old one is gone (the outlet swapped the
     *     component — the dead-click regression fails precisely here);
     *   - a painted marker inside the new view is visible (the template rendered rather than
     *     erroring out to an empty shell).
     */
    test('top-nav HOME and CATALOG buttons change the rendered view', async ({ sdcPage }) => {
        await sdcPage.locator(SEL.catalogButton).click();
        await settles(sdcPage);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).toMatch(/^\/catalog/);
        await expect(sdcPage.locator(SEL.catalogView)).toBeAttached({ timeout: 20_000 });
        await expect(sdcPage.locator(SEL.homeView),
            'the dashboard view is still mounted — CATALOG changed the URL but not the view')
            .toHaveCount(0);
        await expect(sdcPage.locator(SEL.catalogViewMarker).first()).toBeVisible({ timeout: 20_000 });

        await sdcPage.locator(SEL.homeButton).click();
        await settles(sdcPage);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).toMatch(/^\/dashboard/);
        await expect(sdcPage.locator(SEL.homeView)).toBeAttached({ timeout: 20_000 });
        await expect(sdcPage.locator(SEL.catalogView),
            'the catalog view is still mounted — HOME changed the URL but not the view')
            .toHaveCount(0);
        await expect(sdcPage.locator(SEL.homeViewMarker).first()).toBeVisible({ timeout: 20_000 });
    });

    /**
     * Deep-link into a workspace child URL. `cold: true` routes through gotoCold(), which is now a
     * single page.goto() — a real browser navigation, so this exercises the PRE-BOOTSTRAP URL parse
     * (SdcHashLocationStrategy's '!'-tolerant path()) on top of route recognition, the resolver and
     * the guards. Before CR 2 that parse was broken and gotoCold() had to fake it in two steps.
     */
    test('deep-link: loading a workspace child URL lands on that tab', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwDeepLink');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general', cold: true });

        expect(await settles(sdcPage), 'page did not settle after the deep link').toBe(true);
        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 30_000 });
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 15_000 });
        // The id must survive the round trip — PathUtilities.java:256 reads it as the segment
        // immediately after 'workspace', so a reordered URL breaks the Selenium suite.
        expect(await currentRoute(sdcPage)).toContain(`/workspace/${vf.id}/resource`);
    });

    /**
     * F5 KEEPS YOUR PLACE. This test previously pinned the INVERSE — the cold-load defect from
     * dd810f196 (2026-06-24), where Angular's HashLocationStrategy rewrote '#!/x' to '#/!/x'
     * during the Router's initial navigation, destroyed the '#!' prefix and dumped every reload
     * onto otherwise('dashboard'). It was written as a failing-on-fix pin precisely so CR 2 could
     * not ship a still-broken deep link unnoticed; SdcHashLocationStrategy fixed it, the pin went
     * red, and these are the assertions it was inverted to.
     *
     * The route assertion is the load-bearing one: the sidebar could render from a fallback
     * dashboard route too, so only the id in the URL proves the reload was honoured.
     */
    test('page reload keeps you on the same workspace tab', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwReload');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 15_000 });

        await sdcPage.reload();
        expect(await settles(sdcPage), 'page did not settle after reload').toBe(true);

        await expect.poll(() => currentRoute(sdcPage), { timeout: 20_000 })
            .toContain(`/workspace/${vf.id}/resource`);
        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 30_000 });
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 15_000 });
    });

    /**
     * Browser history. Untested before this suite: ui-router and the Angular Router integrate
     * with the history API in entirely different ways, and a back button that leaves the URL and
     * the view disagreeing is a user-visible break that no unit test models.
     */
    test('browser BACK and FORWARD move between routes and re-render', async ({ sdcPage }) => {
        await gotoTopLevel(sdcPage, '/dashboard');
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).toMatch(/^\/dashboard/);

        await sdcPage.locator(SEL.catalogButton).click();
        await settles(sdcPage);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).toMatch(/^\/catalog/);

        await sdcPage.goBack();
        await settles(sdcPage);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).toMatch(/^\/dashboard/);
        // The VIEW must follow the URL back, not just the address bar.
        await expect(sdcPage.locator(SEL.homeView)).toBeAttached({ timeout: 20_000 });
        await expect(sdcPage.locator(SEL.catalogView),
            'BACK moved the URL but left the catalog view mounted').toHaveCount(0);

        await sdcPage.goForward();
        await settles(sdcPage);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).toMatch(/^\/catalog/);
        await expect(sdcPage.locator(SEL.catalogView)).toBeAttached({ timeout: 20_000 });
        await expect(sdcPage.locator(SEL.homeView),
            'FORWARD moved the URL but left the dashboard view mounted').toHaveCount(0);
    });

    /**
     * ui-router's `$urlRouterProvider.otherwise('/dashboard')` becomes a wildcard route after
     * CR 2. Without it an unknown URL renders a blank page rather than the dashboard.
     */
    test('an unknown route falls back to the dashboard', async ({ sdcPage }) => {
        await gotoTopLevel(sdcPage, '/this-route-does-not-exist');
        await settles(sdcPage);

        await expect.poll(() => currentRoute(sdcPage), { timeout: 20_000 }).toMatch(/^\/dashboard/);
        await expect(sdcPage.locator(SEL.homeButton)).toBeVisible({ timeout: 15_000 });
    });

    test('the workspace sidebar is populated for an existing asset', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwSidebar');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await dismissTransientModal(sdcPage);

        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 30_000 });
        // This is the EDIT-mode direction of the bare-vs-dotted menu-state comparison that phase 13
        // CR 2 task 2 re-derives. configurations/menu.js genuinely mixes both forms — 'general',
        // 'properties' and 'tosca_artifacts' are bare while the other 22 are dotted
        // ('workspace.deployment', …) — and disableMenuItems() compares against
        // States.WORKSPACE_GENERAL ('workspace.general'). Get the comparison wrong and tabs are
        // disabled that should not be, which is what >0 enabled items here detects.
        //
        // NOTE the CREATE-mode direction is NOT covered by this test and needs its own: an
        // existing asset is opened here, so enableMenuItems() applies. Asserting on CREATE mode
        // would additionally have to tolerate line 323, which disables the General tab on purpose
        // during create (DE246274).
        await expect(sdcPage.locator(SEL.sideMenuItems)).not.toHaveCount(0);
        const enabled = await sdcPage.locator(
            `${SEL.sideMenuItems} .expand-collapse-menu-box-item-text:not(.disabled)`).count();
        expect(enabled, 'every sidebar tab is disabled — the bare-vs-dotted state comparison regressed')
            .toBeGreaterThan(0);
    });

    /**
     * The exact settle predicate Selenium's AdditionalConditions.pageLoadWait() uses. When this
     * fails, the CI UI job fails with an opaque timeout, so it is worth asserting directly.
     */
    test('the page settles by the Selenium pageLoadWait definition', async ({ sdcPage }) => {
        expect(await settles(sdcPage, 20_000)).toBe(true);
    });
});
