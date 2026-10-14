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
 * Accessibility ratchet. KNOWN lists the axe rules each page still violates. The test fails on any
 * violation not listed AND on any listed rule that no longer fires, so a fix must shorten the list
 * and a regression in one rule cannot hide behind another rule's entry.
 */
import { test, expect, SEL, gotoCold, gotoWorkspaceTab, dismissTransientModal } from './fixtures/sdc';
import { Page } from '@playwright/test';

const TAGS = ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa', 'wcag22aa', 'best-practice'];

type PageName = 'dashboard' | 'catalog' | 'onboardVendor' | 'serviceCreateGeneral';

const KNOWN: Record<PageName, string[]> = {
    dashboard: [],
    catalog: ['scrollable-region-focusable'],
    onboardVendor: ['color-contrast', 'select-name'],
    serviceCreateGeneral: [],
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
};

async function axeRuleIds(page: Page): Promise<{ ids: string[]; detail: string[] }> {
    await page.addScriptTag({ path: require.resolve('axe-core/axe.min.js') });
    return page.evaluate(async (tags) => {
        const r = await (window as any).axe.run(document, { runOnly: { type: 'tag', values: tags } });
        return {
            ids: r.violations.map((v: any) => v.id).sort(),
            detail: r.violations.map((v: any) =>
                `${v.id}: ${v.nodes.slice(0, 5).map((n: any) => n.target.join(' ')).join(' | ')}`),
        };
    }, TAGS);
}

test.describe('accessibility ratchet', () => {
    for (const name of Object.keys(KNOWN) as PageName[]) {
        test(`${name}: no axe violations beyond the known list`, async ({ sdcPage }) => {
            await OPEN[name](sdcPage);
            await dismissTransientModal(sdcPage);
            const { ids, detail } = await axeRuleIds(sdcPage);
            const known = KNOWN[name];
            expect(ids.filter(id => known.indexOf(id) === -1), `new violations:\n${detail.join('\n')}`)
                .toEqual([]);
            expect(known.filter(id => ids.indexOf(id) === -1),
                'these rules pass now: remove them from KNOWN').toEqual([]);
        });
    }
});
