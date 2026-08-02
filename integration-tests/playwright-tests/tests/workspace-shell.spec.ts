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
 * Workspace shell (chrome) regression guards.
 *
 * WHAT THIS GUARDS — four migration failures that were all green in Jest + AOT + Selenium:
 *   1. The shell renders in VIEW/EDIT mode and in CREATE mode (id-less URL).
 *   2. Clicking Create fires the create POST *and* shows the loader. A PS6 regression made the
 *      Create button a dead click: no handler, no loader, and Selenium's waitForLoader() timed
 *      out at createVf with a misleading error.
 *   3. The two EXACT-class xpath contracts that 8 Selenium page objects depend on.
 *   4. Switching sidebar tabs keeps the sidebar mounted and the page settling.
 *
 * HOW TO RUN: see README.md — `npx playwright test workspace-shell`.
 */

import { test, expect, SEL, settles, dismissTransientModal, gotoWorkspaceTab } from './fixtures/sdc';

test.describe('Workspace shell', () => {

    test('VIEW/EDIT mode: shell chrome and the General form render and settle', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwShellEdit');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });

        expect(await settles(sdcPage), 'page did not settle').toBe(true);

        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator('top-nav')).toHaveCount(1);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator(SEL.versionHeader)).toBeVisible({ timeout: 10_000 });
    });

    test('CREATE mode: the id-less workspace URL renders an empty form and the Create button', async ({ sdcPage }) => {
        // Omitting the id yields the DOUBLE-slash form '#!/dashboard/workspace//resource/general'
        // — see the comment on gotoWorkspaceTab() for why the single-slash shape does not work.
        await gotoWorkspaceTab(sdcPage, { type: 'resource', tab: 'general' });

        expect(await settles(sdcPage), 'page did not settle').toBe(true);

        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator(SEL.chromeCreateButton)).toBeVisible({ timeout: 10_000 });
    });

    /**
     * The two EXACT-class contracts. 8 Selenium page objects build their locator as
     * "//div[@class='%s']" with NO contains() — 7 for 'w-sdc-main-right-container' and
     * ResourceWorkspaceTopBarComponent for 'sdc-workspace-top-bar' — so ANY extra class on either
     * element (including Angular's own 'ng-star-inserted' marker, added when a structural
     * directive is hosted on the element) makes them all throw NoSuchElementException. This
     * asserts the class attribute is exactly right, which no Jest test and no AOT build can check.
     */
    test('the EXACT-class Selenium xpath contracts hold on the shell', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwShellXpath');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);

        await expect(sdcPage.locator(SEL.mainRightContainer)).toBeVisible({ timeout: 15_000 });

        for (const [label, sel] of [
            ['w-sdc-main-right-container', SEL.mainRightContainer],
            ['sdc-workspace-top-bar', SEL.topBar],
        ] as const) {
            const cls = await sdcPage.locator(sel).first().getAttribute('class');
            expect(cls, `${label} must carry EXACTLY its own class (found "${cls}")`).toBe(label);
        }
    });

    test('CREATE flow: clicking Create fires the POST and shows the loader', async ({ sdcPage }) => {
        await gotoWorkspaceTab(sdcPage, { type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 15_000 });

        const name = 'PwCreateFlow' + Date.now();
        await sdcPage.locator(SEL.name).fill(name);
        await sdcPage.locator(SEL.description).fill('created by the create-flow guard');
        const cat = sdcPage.locator(SEL.category);
        if (await cat.count()) {
            await cat.selectOption({ index: 1 }).catch(() => { /* category may be pre-selected */ });
        }
        await sdcPage.locator(SEL.vendorName).fill('pwVendor').catch(() => { /* service has no vendor */ });
        await sdcPage.locator(SEL.vendorRelease).fill('1.0').catch(() => { /* ditto */ });

        await dismissTransientModal(sdcPage);

        // A MutationObserver rather than a locator wait: the loader can flash faster than a poll
        // interval, and the subsequent navigation would race a locator assertion.
        await sdcPage.evaluate((sel) => {
            (window as any).__loaderSeen = !!document.querySelector(sel);
            new MutationObserver(() => {
                if (document.querySelector(sel)) { (window as any).__loaderSeen = true; }
            }).observe(document.body, { childList: true, subtree: true });
        }, SEL.loader);

        const createResp = sdcPage.waitForResponse(
            (r) => r.url().includes('/catalog/resources') && r.request().method() === 'POST',
            { timeout: 40_000 });
        await sdcPage.locator(SEL.chromeCreateButton).click();
        const resp = await createResp;

        expect(await sdcPage.evaluate(() => (window as any).__loaderSeen === true),
            'the loader never appeared — Selenium waitForLoader() would time out here').toBe(true);

        // 201 = created and the app navigates to the new asset. A 400 (duplicate name in a shared
        // environment) still proves the handler fired, so only the navigation assertion is conditional.
        if (resp.status() === 201) {
            await expect.poll(() => sdcPage.url(), { timeout: 20_000 })
                .toMatch(/\/workspace\/[0-9a-f-]{36}\/resource/);
        }
    });

    test('tab navigation: switching tabs keeps the sidebar mounted and the page settling', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwShellTabs');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 15_000 });
        await dismissTransientModal(sdcPage);

        // A REAL click, not a URL write: the 2026-07-09 regression was a nav button that
        // highlighted but never changed the view, which only a real click reproduces.
        await sdcPage.locator(SEL.sideMenu('Information Artifact')).click();
        expect(await settles(sdcPage), 'page did not settle after the tab switch').toBe(true);

        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator(SEL.sideMenuItems)).not.toHaveCount(0);
    });
});
