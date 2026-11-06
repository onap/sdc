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
 * CATALOG left panel: type, status and category filters, sorting, search persistence and checking
 * an asset out from the catalog. Each test narrows the shared catalog with a search term unique to
 * the assets it seeded.
 */

import { Page } from '@playwright/test';
import { test, expect, SEL, uniqueName, SdcApi, CreatedAsset } from './fixtures/sdc';
import {
    CAT, TYPE_FILTER, openCatalog, tile, searchCatalog, setFilter, tileNames, lifecycle, metadata, createResource,
} from './fixtures/catalog';
import { createCategory, createSubCategory, categoryName } from './fixtures/admin';

test.describe.configure({ timeout: 300_000 });

/** A search term shared by every asset a test seeds and by nothing else in the catalog. */
const tag = () => uniqueName('Pwcat').slice(2);

async function expectShown(page: Page, shown: CreatedAsset[], hidden: CreatedAsset[]): Promise<void> {
    for (const a of shown) {
        await expect(tile(page, a.name), `${a.name} should be listed`).toBeVisible();
    }
    for (const a of hidden) {
        await expect(tile(page, a.name), `${a.name} should be filtered out`).toHaveCount(0);
    }
}

test.describe('Catalog left panel', () => {

    test('filters by type', async ({ sdcPage, api }) => {
        const t = tag();
        const vf = await createResource(api, 'VF', `Vf${t}`);
        const pnf = await createResource(api, 'PNF', `Pnf${t}`);
        const cr = await createResource(api, 'CR', `Cr${t}`);
        const svc = await api.createService(`Svc${t}`);
        const seeded: Record<string, CreatedAsset> = { VF: vf, PNF: pnf, CR: cr, S: svc };
        const all = Object.values(seeded);

        await openCatalog(sdcPage);
        await searchCatalog(sdcPage, t);
        await expectShown(sdcPage, all, []);

        for (const [code, asset] of Object.entries(seeded)) {
            await setFilter(sdcPage, CAT.type(TYPE_FILTER[code]), true);
            await expectShown(sdcPage, [asset], all.filter(a => a !== asset));
            await setFilter(sdcPage, CAT.type(TYPE_FILTER[code]), false);
        }

        await setFilter(sdcPage, CAT.type('resource'), true);
        await expectShown(sdcPage, [vf, pnf, cr], [svc]);
        await setFilter(sdcPage, CAT.type('resource'), false);

        // No VFC, CP or VL can be created without a TOSCA import, so these use the normative types.
        for (const [code, normative] of [['VFC', 'Compute'], ['CP', 'ExtCP'], ['VL', 'ExtVL']]) {
            await searchCatalog(sdcPage, normative);
            await setFilter(sdcPage, CAT.type(TYPE_FILTER[code]), true);
            await expect(tile(sdcPage, normative)).toBeVisible();
            const types = await sdcPage.locator(`${CAT.tile} [data-asset-type]`)
                .evaluateAll(els => els.map(e => e.getAttribute('data-asset-type')));
            expect(new Set(types), `the ${code} filter let other types through`).toEqual(new Set([code]));
            await setFilter(sdcPage, CAT.type(TYPE_FILTER[code]), false);
            await setFilter(sdcPage, CAT.type(TYPE_FILTER.VF), true);
            await expect(tile(sdcPage, normative)).toHaveCount(0);
            await setFilter(sdcPage, CAT.type(TYPE_FILTER.VF), false);
        }
    });

    test('filters by status', async ({ sdcPage, api }) => {
        const t = tag();
        const inDesign = await api.createVf(`Design${t}`);
        const certified = await lifecycle(api, await api.createVf(`Cert${t}`), 'certify');
        expect((await metadata(api, certified)).lifecycleState).toBe('CERTIFIED');

        await openCatalog(sdcPage);
        await searchCatalog(sdcPage, t);
        await expectShown(sdcPage, [inDesign, certified], []);

        await setFilter(sdcPage, CAT.status('in-design'), true);
        await expectShown(sdcPage, [inDesign], [certified]);
        await setFilter(sdcPage, CAT.status('in-design'), false);

        await setFilter(sdcPage, CAT.status('certified'), true);
        await expectShown(sdcPage, [certified], [inDesign]);
        await setFilter(sdcPage, CAT.status('certified'), false);

        // Neither asset was distributed; a distributed one needs a distribution engine the stack lacks.
        await setFilter(sdcPage, CAT.status('distributed'), true);
        await expectShown(sdcPage, [], [inDesign, certified]);
    });

    test('filters by category and sub-category', async ({ sdcPage, api }) => {
        const t = tag();
        const category = await createCategory(api, 'resource', categoryName('Pwfilter'));
        const sub = await createSubCategory(api, category, categoryName('Pwfsub'));
        const inNew = await createResource(api, 'VF', `New${t}`, {
            name: category.name, uniqueId: category.uniqueId, sub: sub.name, subId: sub.uniqueId,
        });
        const inGeneric = await createResource(api, 'VF', `Generic${t}`);

        await openCatalog(sdcPage);
        await searchCatalog(sdcPage, t);
        await expectShown(sdcPage, [inNew, inGeneric], []);

        await setFilter(sdcPage, CAT.category(category.name), true);
        await expectShown(sdcPage, [inNew], [inGeneric]);
        await setFilter(sdcPage, CAT.category(category.name), false);

        await setFilter(sdcPage, CAT.category(category.name, sub.name), true);
        await expectShown(sdcPage, [inNew], [inGeneric]);
        await setFilter(sdcPage, CAT.category(category.name, sub.name), false);

        await setFilter(sdcPage, CAT.category('Generic', 'Abstract'), true);
        await expectShown(sdcPage, [inGeneric], [inNew]);
    });

    test('sorts by last update, newest first, and reverses on a second click', async ({ sdcPage, api }) => {
        const t = tag();
        const older = await api.createVf(`Older${t}`);
        const newer = await api.createService(`Newer${t}`);
        const [o, n] = [await metadata(api, older), await metadata(api, newer)];
        expect(n.lastUpdateDate, 'the seeds share a timestamp').toBeGreaterThan(o.lastUpdateDate);

        await openCatalog(sdcPage);
        await searchCatalog(sdcPage, t);
        await expect.poll(() => tileNames(sdcPage)).toEqual([newer.name, older.name]);
        await expect(sdcPage.locator(CAT.sortByUpdate)).toHaveAttribute('aria-pressed', 'true');

        await sdcPage.locator(CAT.sortByUpdate).click();
        await expect.poll(() => tileNames(sdcPage)).toEqual([older.name, newer.name]);
    });

    test('keeps the search term after browser Back from a workspace', async ({ sdcPage, api }) => {
        const vf = await api.createVf(`Back${tag()}`);

        await openCatalog(sdcPage);
        await searchCatalog(sdcPage, vf.name);
        await expect(sdcPage.locator(CAT.header)).toHaveText('1 Active Element found');
        await tile(sdcPage, vf.name).locator('.sdc-tile-content-icon').click();
        await expect(sdcPage.locator(SEL.name)).toHaveValue(vf.name, { timeout: 30_000 });

        await sdcPage.goBack();

        await expect(sdcPage.locator(CAT.search)).toHaveValue(vf.name, { timeout: 60_000 });
        await expect(sdcPage.locator(CAT.header)).toHaveText('1 Active Element found');
        expect(await tileNames(sdcPage)).toEqual([vf.name]);
    });

    test('checks out an asset opened from the catalog', async ({ sdcPage, api }) => {
        const vf = await lifecycle(api, await api.createVf(`Checkout${tag()}`), 'checkin');
        expect((await metadata(api, vf)).lifecycleState).toBe('NOT_CERTIFIED_CHECKIN');

        await openCatalog(sdcPage);
        await searchCatalog(sdcPage, vf.name);
        await tile(sdcPage, vf.name).locator('.sdc-tile-content-icon').click();
        const checkout = sdcPage.waitForResponse(r => r.request().method() === 'POST'
            && /\/lifecycleState\/checkout$/i.test(r.url()));
        await sdcPage.locator('[data-tests-id="check_out"]').click();

        const resp = await checkout;
        expect(resp.status()).toBe(200);
        const checkedOut = { ...vf, id: (await resp.json()).uniqueId };
        expect((await metadata(api, checkedOut)).lifecycleState).toBe('NOT_CERTIFIED_CHECKOUT');
        await expect(sdcPage.locator('[data-tests-id="formlifecyclestate"]')).toHaveText(/In Design Check Out/i);
    });
});
