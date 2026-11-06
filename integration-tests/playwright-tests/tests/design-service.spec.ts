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
 * Editing a service's General tab: every field the Selenium update scenario changed is saved and
 * shown again after a cold reload.
 */

import { test, expect, SEL, gotoWorkspaceTab, loaderGone, uniqueName } from './fixtures/sdc';
import { createTaggedService, getComponent } from './fixtures/design';

const METADATA_PUT = /\/v1\/catalog\/services\/[^/]+\/metadata$/;

test.describe('Service General tab', () => {
    test.use({ actionTimeout: 20_000 });

    test('updating name, description, category, contact, naming policy, function and tags is stored', async ({ sdcPage: page, api }) => {
        const svc = await createTaggedService(api, 'PwDesignSvc', ['serviceTag', 'serviceTag1']);
        const update = {
            name: uniqueName('PwDesignSvcUpdated'),
            description: 'updatedDescriptionSanity',
            category: 'VoIP Call Control',
            contactId: 'cs6543',
            namingPolicy: 'updatedNamingPolicy',
            serviceFunction: 'updatedServiceFunction',
            tags: ['updatedTag', 'oneMoreUpdatedTag', 'lastOne UpdatedTag'],
        };

        await gotoWorkspaceTab(page, { id: svc.id, type: 'service', tab: 'general' });
        await loaderGone(page);
        const tagDelete = page.locator('[data-tests-id="i-sdc-tag-delete"]');
        await expect(tagDelete).not.toHaveCount(0);
        while (await tagDelete.count()) {
            await tagDelete.first().click();
        }
        await page.locator(SEL.name).fill(update.name);
        await page.locator(SEL.description).fill(update.description);
        await page.locator(SEL.category).selectOption({ label: update.category });
        await page.locator('[data-tests-id="contactId"]').fill(update.contactId);
        await page.locator('[data-tests-id="namingPolicy"]').fill(update.namingPolicy);
        await page.locator('[data-tests-id="serviceFunction"]').fill(update.serviceFunction);
        // The service name is a tag the form keeps by itself, so it is not typed here.
        for (const tag of update.tags) {
            await page.locator('[data-tests-id="i-sdc-tag-input"]').fill(tag);
            await page.locator('[data-tests-id="i-sdc-tag-input"]').press('Enter');
        }
        const saved = page.waitForResponse((r) => r.request().method() === 'PUT' && METADATA_PUT.test(r.url()), { timeout: 30_000 });
        await page.locator(SEL.formSaveButton).click();
        const resp = await saved;
        expect(resp.ok(), `the update was rejected: ${(await resp.text()).slice(0, 400)}`).toBe(true);

        const { metadata } = await getComponent(api, svc, 'metadata');
        expect(metadata.name).toBe(update.name);
        expect(metadata.description).toBe(update.description);
        expect(metadata.categories.map((c: any) => c.name)).toEqual([update.category]);
        expect(metadata.contactId).toBe(update.contactId);
        expect(metadata.namingPolicy).toBe(update.namingPolicy);
        expect(metadata.serviceFunction).toBe(update.serviceFunction);
        expect([...metadata.tags].sort()).toEqual([...update.tags, update.name].sort());

        await gotoWorkspaceTab(page, { id: svc.id, type: 'service', tab: 'general', cold: true });
        await loaderGone(page);
        await expect(page.locator(SEL.name)).toHaveValue(update.name);
        await expect(page.locator(SEL.description)).toHaveValue(update.description);
        await expect(page.locator(`${SEL.category} option:checked`)).toHaveText(update.category);
        await expect(page.locator('[data-tests-id="contactId"]')).toHaveValue(update.contactId);
        await expect(page.locator('[data-tests-id="namingPolicy"]')).toHaveValue(update.namingPolicy);
        await expect(page.locator('[data-tests-id="serviceFunction"]')).toHaveValue(update.serviceFunction);
        expect((await page.locator('[data-tests-id="i-sdc-tag-text"]').allInnerTexts()).map(t => t.trim()).sort())
            .toEqual([...update.tags, update.name].sort());
    });
});
