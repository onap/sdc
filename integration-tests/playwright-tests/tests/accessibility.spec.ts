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
 * Accessibility ratchet over pages that need no data of their own. KNOWN lists the axe rules each page
 * still violates; see expectAxeRatchet() for the two-way comparison. Read-only: the data-writing
 * halves are accessibility-workspace.spec.ts and accessibility-onboarding.spec.ts.
 */
import { test, expect, SEL, ADMIN_USER, gotoCold, gotoWorkspaceTab, expectAxeRatchet } from './fixtures/sdc';
import { Page } from '@playwright/test';

type PageName = 'dashboard' | 'catalog' | 'onboardVendor' | 'serviceCreateGeneral' | 'vfCreateGeneral';

const KNOWN: Record<PageName, string[]> = {
    dashboard: [],
    catalog: [],
    onboardVendor: [],
    serviceCreateGeneral: [],
    vfCreateGeneral: [],
};

const OPEN: Record<PageName, (p: Page) => Promise<void>> = {
    // gotoCold() returns while index.html's splash can still fill <app-root>, and axe then reports
    // the splash (image-alt, landmark-one-main, page-has-heading-one, ...) instead of the page. So
    // each page waits for content that only the bootstrapped view renders.
    dashboard: async p => {
        await gotoCold(p, '/dashboard');
        await p.locator(SEL.homeButton).waitFor({ timeout: 30_000 });
    },
    // The tiles, not just the view: scrollable-region-focusable fires only once they overflow it.
    catalog: async p => {
        await gotoCold(p, '/catalog');
        await p.locator(`${SEL.catalogView} ui-tile`).first().waitFor({ timeout: 60_000 });
    },
    onboardVendor: async p => {
        await gotoCold(p, '/onboardVendor');
        await p.locator('[data-test-id="catalog-filter-items-type"]').waitFor({ timeout: 60_000 });
    },
    serviceCreateGeneral: p => gotoWorkspaceTab(p, { type: 'service', tab: 'general', cold: true }),
    vfCreateGeneral: p => gotoWorkspaceTab(p, { type: 'resource', tab: 'general', cold: true }),
};

test.describe('accessibility ratchet', () => {
    for (const name of Object.keys(KNOWN) as PageName[]) {
        test(`${name}: no axe violations beyond the known list`, async ({ sdcPage }) => {
            await OPEN[name](sdcPage);
            await expectAxeRatchet(sdcPage, KNOWN[name]);
        });
    }
});

test.describe('accessibility ratchet (admin)', () => {
    test.use({ sdcUser: ADMIN_USER });

    test('adminDashboard: no axe violations beyond the known list', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/adminDashboard');
        const firstRow = sdcPage.locator('[data-tests-id="row_0"]');
        await firstRow.waitFor({ timeout: 30_000 });
        // Row 0 always plays the 7 s new-user highlight (#28bd6e on white, 2.44:1). Scan the steady
        // state, or the result depends on how fast the page loaded.
        await expect.poll(() => firstRow.evaluate(row =>
            row.getAnimations().every(a => a.playState === 'finished')), { timeout: 15_000 }).toBe(true);
        await expectAxeRatchet(sdcPage, []);
    });
});
