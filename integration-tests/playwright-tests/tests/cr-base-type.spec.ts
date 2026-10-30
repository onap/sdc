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

import { Page } from '@playwright/test';
import { test, expect, login, stubPluginsConfig, settles, IS_DEV_SERVER } from './fixtures/sdc';

const VF_TYPE = 'org.openecomp.resource.abstract.nodes.VF';
const CR_TYPE = 'org.openecomp.resource.abstract.nodes.CR';
const CATEGORY = 'Generic_#_Abstract';
const CREATE_POST = /\/v1\/catalog\/resources$/;

async function openAddCrForm(page: Page): Promise<void> {
    if (IS_DEV_SERVER) {
        await stubPluginsConfig(page);
    }
    await login(page);
    await page.locator('.w-sdc-dashboard-card-new').first().hover();
    await page.locator('[data-tests-id="createCRButton"]').click();
    await expect(page.locator('[data-tests-id="selectGeneralCategory"]')).toBeVisible({ timeout: 30_000 });
    await settles(page);
}

test.describe('CR Base Type', () => {

    for (const scenario of [
        {name: 'runtime suggestion', value: VF_TYPE, discovered: true, base: VF_TYPE, input: 'nf_role', absent: 'cr_role'},
        {name: 'manual name absent from discovery', value: VF_TYPE, discovered: false, base: VF_TYPE, input: 'nf_role', absent: 'cr_role'},
        {name: 'backend default', value: '', discovered: true, base: CR_TYPE, input: 'cr_role', absent: 'nf_role'}
    ]) {
    test(`creates the CR using ${scenario.name}`, async ({ page, api }) => {
        if (!scenario.discovered) {
            await page.route('**/v1/catalog/nodeTypes*', route => route.fulfill({
                status: 200, contentType: 'application/json', body: '{}'
            }));
        }
        await openAddCrForm(page);
        await expect(page.getByLabel('Base Type', { exact: true })).toBeVisible();
        if (scenario.discovered) {
            await expect(page.locator(`#gt-cr-base-types option[value="${VF_TYPE}"]`)).toHaveCount(1);
        } else {
            await expect(page.locator('#gt-cr-base-types option')).toHaveCount(0);
        }

        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await page.getByLabel('Base Type', { exact: true }).fill(VF_TYPE);
        await page.getByLabel('Base Type', { exact: true }).fill(scenario.value);

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
        expect(sent.derivedFromGenericType).toBe(scenario.value || undefined);
        expect(sent.derivedFromGenericVersion).toBeUndefined();

        const body = await resp.json();
        try {
            expect(resp.status(), JSON.stringify(body).slice(0, 400)).toBe(201);
            expect(body.derivedFromGenericType).toBe(scenario.base);
            const inputs = (body.inputs || []).map((input: any) => input.name);
            expect(inputs).toContain(scenario.input);
            expect(inputs).not.toContain(scenario.absent);
        } finally {
            if (body.uniqueId) {
                await api.deleteAsset({ id: body.uniqueId, name, type: 'resource' });
            }
        }
    });
    }

    test('preserves the chosen type when the category changes', async ({ page }) => {
        await openAddCrForm(page);
        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await page.getByLabel('Base Type', { exact: true }).fill(VF_TYPE);

        const other = await page.locator('[data-tests-id="selectGeneralCategory"] option')
            .evaluateAll((options: HTMLOptionElement[]) =>
                options.map(o => o.value).find(v => v.indexOf('_#_') > 0 && !v.startsWith('Generic_#_')));
        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(other);
        await expect(page.getByLabel('Base Type', { exact: true })).toHaveValue(VF_TYPE);

        await page.locator('[data-tests-id="selectGeneralCategory"]').selectOption(CATEGORY);
        await expect(page.getByLabel('Base Type', { exact: true })).toHaveValue(VF_TYPE);
    });

    test('retains manual entry and default when discovery fails, and can retry', async ({ page }) => {
        await page.route('**/v1/catalog/nodeTypes*', route => route.fulfill({
            status: 503, contentType: 'application/json', body: '{}'
        }));
        await openAddCrForm(page);
        await expect(page.getByRole('alert')).toContainText('Could not load base types.');
        await page.getByLabel('Base Type', { exact: true }).fill('example.nodes.CustomBase');
        await expect(page.getByLabel('Base Type', { exact: true })).toHaveValue('example.nodes.CustomBase');
        await page.getByLabel('Base Type', { exact: true }).fill('');
        await expect(page.getByLabel('Base Type', { exact: true })).toHaveValue('');
        await page.unroute('**/v1/catalog/nodeTypes*');
        await page.getByRole('button', { name: 'Retry', exact: true }).click();
        await expect(page.locator(`#gt-cr-base-types option[value="${VF_TYPE}"]`)).toHaveCount(1);
        await expect(page.getByRole('alert')).toHaveCount(0);
    });
});
