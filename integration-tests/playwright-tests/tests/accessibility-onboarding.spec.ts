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
 * Accessibility ratchet over the onboarding (openecomp-ui punch-out) inner pages.
 * CREATES DATA (a submitted VLM, and a VSP under it, per test): never point it at a shared cluster.
 * The punch-out has no URL routing (the hash stays #!/onboardVendor), so pages are reached by clicking.
 * KNOWN semantics as in accessibility.spec.ts: see expectAxeRatchet().
 */
import { test, expect, gotoCold, expectAxeRatchet, loaderGone, SdcApi, OnboardingItem } from './fixtures/sdc';
import { Page } from '@playwright/test';

const OB = {
    filters: '[data-test-id="catalog-filter-items-type"]',
    workspaceTab: '[data-test-id="onboard-workspace-tab"]',
    onboardCatalogTab: '[data-test-id="onboard-onboard-tab"]',
    itemName: '[data-test-id="catalog-item-name"]',
    selectVersion: '[data-test-id="versions-page-select-version"]',
    versionsPageLink: '[data-test-id="vc-versions-page-link"]',
    nav: (screen: string) => `[data-test-id="navbar-group-item-${screen}"]`,
};

async function openOnboardTab(p: Page, tab: string, name: string): Promise<void> {
    await gotoCold(p, '/onboardVendor');
    await p.locator(OB.filters).waitFor({ timeout: 60_000 });
    await p.locator(tab).click();
    await p.locator(OB.itemName, { hasText: name }).first().click();
}

async function selectScreen(p: Page, landing: string, screen: string, ready: string): Promise<void> {
    await expect(p.locator(`${OB.nav(landing)}.selected`)).toBeVisible({ timeout: 30_000 });
    if (screen !== landing) {
        await p.locator(OB.nav(screen)).click();
        await expect(p.locator(`${OB.nav(screen)}.selected`)).toBeVisible({ timeout: 30_000 });
    }
    await p.locator(ready).first().waitFor({ state: 'attached', timeout: 30_000 });
    await loaderGone(p);
}

/** ONBOARD CATALOG lists only items with a submitted version, and always opens the versions page. */
async function openVlmVersionsPage(p: Page, name: string): Promise<void> {
    await openOnboardTab(p, OB.onboardCatalogTab, name);
    await p.locator(OB.selectVersion).first().waitFor({ timeout: 30_000 });
    await loaderGone(p);
}

/** WORKSPACE opens an item with a single draft straight on that draft, skipping the versions page. */
async function openVspDraft(p: Page, name: string): Promise<void> {
    await openOnboardTab(p, OB.workspaceTab, name);
    await expect(p.locator(`${OB.nav(VSP_LANDING)}.selected`)).toBeVisible({ timeout: 30_000 });
}

async function submittedVlm(api: SdcApi): Promise<OnboardingItem> {
    const vlm = await api.createVlm('PwAxeVlm');
    await api.submitVlm(vlm);
    return vlm;
}

interface ScreenScan {
    screen: string;
    ready: string;
    known: string[];
}

const VLM_LANDING = 'LICENSE_MODEL_OVERVIEW';
const VSP_LANDING = 'SOFTWARE_PRODUCT_LANDING_PAGE';
const ACTIVITY_READY = '[data-test-id="activity-list-item"]:not(.header)';

const VLM_SCREENS: ScreenScan[] = [
    { screen: VLM_LANDING, ready: '[data-test-id="vlm-list"]', known: [] },
    { screen: 'LICENSE_AGREEMENTS', ready: '.list-editor-view-header', known: [] },
    { screen: 'ACTIVITY_LOG', ready: ACTIVITY_READY, known: [] },
];

const VSP_SCREENS: ScreenScan[] = [
    { screen: VSP_LANDING, ready: '.software-product-landing-wrapper', known: [] },
    { screen: 'SOFTWARE_PRODUCT_DETAILS', ready: '[data-test-id="vsp-name"]', known: [] },
    { screen: 'SOFTWARE_PRODUCT_ACTIVITY_LOG', ready: ACTIVITY_READY, known: [] },
];

test.describe('accessibility ratchet: onboarding', () => {
    test('vlm versions page: no axe violations beyond the known list', async ({ sdcPage, api }) => {
        const vlm = await submittedVlm(api);
        await openVlmVersionsPage(sdcPage, vlm.name);
        await expectAxeRatchet(sdcPage, []);
    });

    for (const s of VLM_SCREENS) {
        test(`vlm ${s.screen}: no axe violations beyond the known list`, async ({ sdcPage, api }) => {
            const vlm = await submittedVlm(api);
            await openVlmVersionsPage(sdcPage, vlm.name);
            await sdcPage.locator(OB.selectVersion).first().click();
            await selectScreen(sdcPage, VLM_LANDING, s.screen, s.ready);
            await expectAxeRatchet(sdcPage, s.known);
        });
    }

    test('vsp versions page: no axe violations beyond the known list', async ({ sdcPage, api }) => {
        const vsp = await api.createVsp(await submittedVlm(api), 'PwAxeVsp');
        await openVspDraft(sdcPage, vsp.name);
        await sdcPage.locator(OB.versionsPageLink).click();
        await sdcPage.locator(OB.selectVersion).first().waitFor({ timeout: 30_000 });
        await loaderGone(sdcPage);
        await expectAxeRatchet(sdcPage, []);
    });

    for (const s of VSP_SCREENS) {
        test(`vsp ${s.screen}: no axe violations beyond the known list`, async ({ sdcPage, api }) => {
            const vsp = await api.createVsp(await submittedVlm(api), 'PwAxeVsp');
            await openVspDraft(sdcPage, vsp.name);
            await selectScreen(sdcPage, VSP_LANDING, s.screen, s.ready);
            await expectAxeRatchet(sdcPage, s.known);
        });
    }
});
