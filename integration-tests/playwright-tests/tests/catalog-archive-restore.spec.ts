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
 * Archiving and restoring assets, and what an archived asset or an archived VSP blocks: a service
 * holding an archived VF and a VF whose VSP is archived both refuse certification.
 */

import { APIResponse, Page } from '@playwright/test';
import { test, expect, gotoWorkspaceTab, loaderGone, CreatedAsset } from './fixtures/sdc';
import {
    CAT, openCatalog, tile, searchCatalog, lifecycle, metadata, archive, addInstance, vspAction, onboardedVf,
} from './fixtures/catalog';

test.describe.configure({ timeout: 360_000 });

const ARCHIVED_ORIGINS = 'includes archived resources';

async function openArchiveCatalog(page: Page): Promise<void> {
    await openCatalog(page);
    await page.locator(CAT.selectorToggle).click();
    await page.locator(CAT.selectorItem).filter({ hasText: 'Archive' }).click();
    await expect(page.locator(CAT.header)).toContainText('Archived');
}

/** Clicks Certify, confirms the comment dialog and resolves with the lifecycle response. */
async function certifyViaUi(page: Page): Promise<APIResponse> {
    const certify = page.waitForResponse(r => r.request().method() === 'POST'
        && /\/lifecycleState\/certify$/i.test(r.url()), { timeout: 120_000 });
    await page.locator('[data-tests-id="certify"]').click();
    await page.locator('textarea[data-tests-id="checkindialog"]').fill('certified by the Playwright suite');
    await page.locator('[data-tests-id="confirm-modal-button-ok"]').click();
    return certify;
}

async function certifiedVfInService(api: any): Promise<{ vf: CreatedAsset; svc: CreatedAsset }> {
    const vf = await lifecycle(api, await api.createVf('PwArchVf'), 'certify');
    const svc = await api.createService('PwArchSvc');
    await addInstance(api, svc, vf);
    return { vf, svc };
}

test.describe('Archive and restore', () => {

    test('a VF archived from the top bar is listed in the Archive catalog and blocks certifying its service',
        async ({ sdcPage, api }) => {
            const { vf, svc } = await certifiedVfInService(api);

            await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', cold: true });
            await loaderGone(sdcPage);
            const archived = sdcPage.waitForResponse(r => r.request().method() === 'POST' && r.url().endsWith(`/${vf.id}/archive`));
            await sdcPage.locator('[data-tests-id="archive-component-button"]').click();
            expect((await archived).status()).toBe(200);
            expect((await metadata(api, vf)).isArchived).toBe(true);

            await openArchiveCatalog(sdcPage);
            await searchCatalog(sdcPage, vf.name);
            await expect(tile(sdcPage, vf.name)).toBeVisible();

            await gotoWorkspaceTab(sdcPage, { id: svc.id, type: 'service', cold: true });
            await loaderGone(sdcPage);
            const resp = await certifyViaUi(sdcPage);
            expect(resp.status()).toBe(403);
            await expect(sdcPage.getByRole('dialog', { name: 'Error' })).toContainText(ARCHIVED_ORIGINS);
            expect((await metadata(api, svc)).lifecycleState).not.toBe('CERTIFIED');
        });

    test('a VF restored from the Archive catalog no longer blocks certifying its service', async ({ sdcPage, api }) => {
        const { vf, svc } = await certifiedVfInService(api);
        await archive(api, vf, 'archive');

        await openArchiveCatalog(sdcPage);
        await searchCatalog(sdcPage, vf.name);
        await tile(sdcPage, vf.name).locator('.sdc-tile-content-icon').click();
        const restored = sdcPage.waitForResponse(r => r.request().method() === 'POST' && r.url().endsWith(`/${vf.id}/restore`));
        await sdcPage.locator('[data-tests-id="restore-component-button"]').click();
        expect((await restored).status()).toBe(200);
        expect((await metadata(api, vf)).isArchived).toBe(false);

        await gotoWorkspaceTab(sdcPage, { id: svc.id, type: 'service', cold: true });
        await loaderGone(sdcPage);
        const resp = await certifyViaUi(sdcPage);
        expect(resp.status()).toBe(200);
        expect((await metadata(api, { ...svc, id: (await resp.json()).uniqueId })).lifecycleState).toBe('CERTIFIED');
    });

    test('a VF whose VSP is archived cannot be certified until the VSP is restored', async ({ sdcPage, api }) => {
        const { vsp, vf: draft } = await onboardedVf(api);
        const v1 = await lifecycle(api, draft, 'certify');
        await vspAction(api, vsp, 'ARCHIVE');
        await expect.poll(async () => (await metadata(api, v1)).isVspArchived, { timeout: 30_000 }).toBe(true);

        // SDC-5248: Check Out before the workspace has loaded /v1/followed throws in the checkout
        // handler and leaves the loader up for good.
        const followed = sdcPage.waitForResponse(r => r.url().endsWith('/v1/followed'), { timeout: 120_000 });
        await gotoWorkspaceTab(sdcPage, { id: v1.id, type: 'resource', cold: true });
        await followed;
        await loaderGone(sdcPage);
        const checkout = sdcPage.waitForResponse(r => r.request().method() === 'POST' && /\/lifecycleState\/checkout$/i.test(r.url()));
        await sdcPage.locator('[data-tests-id="check_out"]').click();
        const v2 = { ...v1, id: (await (await checkout).json()).uniqueId };
        expect((await metadata(api, v2)).isVspArchived).toBe(true);

        const refused = await certifyViaUi(sdcPage);
        expect(refused.status()).toBe(403);
        await expect(sdcPage.getByRole('dialog', { name: 'Error' })).toContainText(ARCHIVED_ORIGINS);
        expect((await metadata(api, v2)).lifecycleState).not.toBe('CERTIFIED');

        await vspAction(api, vsp, 'RESTORE');
        await expect.poll(async () => (await metadata(api, v2)).isVspArchived, { timeout: 30_000 }).toBe(false);
        await gotoWorkspaceTab(sdcPage, { id: v2.id, type: 'resource', cold: true });
        await loaderGone(sdcPage);
        const certified = await certifyViaUi(sdcPage);
        expect(certified.status()).toBe(200);
        const meta = await metadata(api, { ...v2, id: (await certified.json()).uniqueId });
        expect([meta.lifecycleState, meta.version, meta.isVspArchived]).toEqual(['CERTIFIED', '2.0', false]);
    });

    test('the General tab of a VF whose VSP is archived says so', async ({ sdcPage, api }) => {
        test.fail(true, 'SDC-5247: the General tab no longer shows "VSP is archived"');
        const { vsp, vf } = await onboardedVf(api);
        await vspAction(api, vsp, 'ARCHIVE');
        await expect.poll(async () => (await metadata(api, vf)).isVspArchived, { timeout: 30_000 }).toBe(true);

        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', cold: true });
        await loaderGone(sdcPage);
        await expect(sdcPage.locator('[data-tests-id="filename"]')).toHaveText('VSP is archived', { timeout: 15_000 });
    });
});
