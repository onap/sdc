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
 * The Base Type select on the Add CR form, driven by the frontend's optional crBaseTypes setting.
 *
 * The setting is stubbed rather than deployed, so the CI stack's own workspace-configuration.yaml
 * stays as Selenium sees it. The offered type is the normative generic VF: it exists in every stack
 * and its properties (nf_role, …) differ from the generic CR's (cr_role, …), so the created CR's
 * inputs prove which type the backend actually derived it from.
 */

import { Page } from '@playwright/test';
import { test, expect, login, stubPluginsConfig, settles, IS_DEV_SERVER } from './fixtures/sdc';

const VF_TYPE = 'org.openecomp.resource.abstract.nodes.VF';
const CATEGORY = 'Generic_#_Abstract';
const CR_SELECT = '[data-tests-id="selectCrBaseType"]';
const CREATE_POST = /\/v1\/catalog\/resources$/;

async function openAddCrForm(page: Page, crBaseTypes?: object): Promise<void> {
    // Before login: the setting is read once, at bootstrap.
    if (IS_DEV_SERVER) {
        await stubPluginsConfig(page);
    }
    if (crBaseTypes) {
        await page.route('**/sdc1/rest/config/ui/workspace', (route) =>
            route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ crBaseTypes }) }));
    }
    await login(page);
    await page.locator('.w-sdc-dashboard-card-new').first().hover();
    await page.locator('[data-tests-id="createCRButton"]').click();
    await expect(page.locator('[data-tests-id="selectGeneralCategory"]')).toBeVisible({ timeout: 30_000 });
    await settles(page);
}

test.describe('CR Base Type', () => {

    test('creates the CR from the chosen base type', async ({ page, api }) => {
        await openAddCrForm(page, { Generic: [VF_TYPE] });
        await expect(page.locator(CR_SELECT)).toHaveCount(0);

        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await expect(page.locator(`${CR_SELECT} option`)).toHaveText(['Default', VF_TYPE]);
        await page.locator(CR_SELECT).selectOption(VF_TYPE);

        const name = 'PwCrBaseType' + Date.now();
        await page.locator('[data-tests-id="name"]').fill(name);
        await page.locator('[data-tests-id="description"]').fill('created by the Playwright suite');
        await page.locator('[data-tests-id="vendorName"]').fill('pwVendor');
        await page.locator('[data-tests-id="vendorRelease"]').fill('1.0');

        const created = page.waitForResponse((resp) =>
            resp.request().method() === 'POST' && CREATE_POST.test(resp.url()), { timeout: 30_000 });
        await page.locator('[data-tests-id="create/save"]').click();
        const resp = await created;

        const sent = resp.request().postDataJSON();
        expect(sent.derivedFromGenericType).toBe(VF_TYPE);
        expect(sent.derivedFromGenericVersion).toBeUndefined();

        const body = await resp.json();
        try {
            expect(resp.status(), JSON.stringify(body).slice(0, 400)).toBe(201);
            expect(body.derivedFromGenericType).toBe(VF_TYPE);
            const inputs = (body.inputs || []).map((input: any) => input.name);
            expect(inputs).toContain('nf_role');
            expect(inputs).not.toContain('cr_role');
        } finally {
            if (body.uniqueId) {
                await api.deleteAsset({ id: body.uniqueId, name, type: 'resource' });
            }
        }
    });

    test('drops the chosen type when the category changes to an unlisted one', async ({ page }) => {
        await openAddCrForm(page, { Generic: [VF_TYPE] });
        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await page.locator(CR_SELECT).selectOption(VF_TYPE);

        const other = await page.locator('[data-tests-id="selectGeneralCategory"] option')
            .evaluateAll((options: HTMLOptionElement[]) =>
                options.map(o => o.value).find(v => v.indexOf('_#_') > 0 && !v.startsWith('Generic_#_')));
        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(other);
        await expect(page.locator(CR_SELECT)).toHaveCount(0);

        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await expect(page.locator(CR_SELECT)).toHaveValue('');
    });

    test('offers no base type without the setting', async ({ page }) => {
        await openAddCrForm(page);
        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await expect(page.locator('[data-tests-id="selectGeneralCategory"]')).toHaveValue(CATEGORY);
        await expect(page.locator(CR_SELECT)).toHaveCount(0);
    });
});
