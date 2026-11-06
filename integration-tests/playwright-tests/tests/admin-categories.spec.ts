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
 * Category management on the admin dashboard, and that a new category reaches the designer's
 * create form.
 */

import { Page } from '@playwright/test';
import {
    test, expect, SEL, ADMIN_USER, DESIGNER_USER, login, gotoWorkspaceTab, loaderGone, uniqueName, SdcApi,
} from './fixtures/sdc';
import {
    ADMIN, CategoryType, categoryName, categoriesNamed, createCategory, createSubCategory,
    openCategoryManagement, submitCategoryModal, addCategoryViaUi, addSubCategoryViaUi,
} from './fixtures/admin';

test.describe('Category management', () => {
    test.use({ sdcUser: ADMIN_USER });

    for (const type of ['resource', 'service'] as CategoryType[]) {
        test(`creates a ${type} category`, async ({ sdcPage, api }) => {
            const name = categoryName(type === 'resource' ? 'Pwrescat' : 'Pwsvccat');
            await openCategoryManagement(sdcPage, type);

            await sdcPage.locator(ADMIN.newCategory).click();
            const resp = await submitCategoryModal(sdcPage, name);

            expect(resp.status()).toBe(201);
            await expect(sdcPage.locator('.custom-modal')).toHaveCount(0);
            await expect(sdcPage.locator(ADMIN.categoryItem(type)).filter({ hasText: name })).toBeVisible();
            expect(await categoriesNamed(api, type, name), `${name} is not stored as a ${type} category`).toHaveLength(1);
        });

        test(`refuses a duplicate ${type} category name with an error modal`, async ({ sdcPage, api }) => {
            const name = categoryName('Pwdupcat');
            await openCategoryManagement(sdcPage, type);
            await addCategoryViaUi(sdcPage, type, name);

            await sdcPage.locator(ADMIN.newCategory).click();
            const resp = await submitCategoryModal(sdcPage, name);

            expect(resp.status()).toBe(409);
            const label = type === 'resource' ? 'Resource' : 'Service';
            await expect(sdcPage.locator(ADMIN.errorModal))
                .toContainText(`${label} category name '${name}' already exists`);
            expect(await categoriesNamed(api, type, name)).toHaveLength(1);
        });
    }

    test('creates a resource sub-category', async ({ sdcPage, api }) => {
        const parent = { name: categoryName('Pwparent') };
        const name = categoryName('Pwsubcat');
        await openCategoryManagement(sdcPage, 'resource');
        await addCategoryViaUi(sdcPage, 'resource', parent.name);

        await sdcPage.locator(ADMIN.categoryItem('resource')).filter({ hasText: parent.name }).click();
        await sdcPage.locator(ADMIN.newSubCategory).click();
        const resp = await submitCategoryModal(sdcPage, name);

        expect(resp.status()).toBe(201);
        await expect(sdcPage.locator(ADMIN.subCategoryItem).filter({ hasText: name })).toBeVisible();
        const [stored] = await categoriesNamed(api, 'resource', parent.name);
        expect((stored.subcategories || []).map(s => s.name)).toEqual([name]);
    });

    test('refuses a duplicate sub-category under the same category', async ({ sdcPage, api }) => {
        const parent = { name: categoryName('Pwparent') };
        const sub = { name: categoryName('Pwsubcat') };
        await openCategoryManagement(sdcPage, 'resource');
        await addCategoryViaUi(sdcPage, 'resource', parent.name);
        await addSubCategoryViaUi(sdcPage, parent.name, sub.name);

        await sdcPage.locator(ADMIN.newSubCategory).click();
        const resp = await submitCategoryModal(sdcPage, sub.name);

        expect(resp.status()).toBe(409);
        await expect(sdcPage.locator(ADMIN.errorModal))
            .toContainText(`sub-category '${sub.name}' already exists under '${parent.name}' category`);
        const [stored] = await categoriesNamed(api, 'resource', parent.name);
        expect((stored.subcategories || []).map(s => s.name)).toEqual([sub.name]);
    });
});

/** A second browser context: the designer's form reads the category list once, at login. */
async function asDesigner(browser: import('@playwright/test').Browser, body: (page: Page) => Promise<void>): Promise<void> {
    const context = await browser.newContext();
    try {
        const page = await context.newPage();
        await login(page, DESIGNER_USER);
        await body(page);
    } finally {
        await context.close();
    }
}

async function createFromForm(page: Page, api: SdcApi, type: 'resource' | 'service', namePrefix: string) {
    const name = uniqueName(namePrefix);
    await page.locator(SEL.name).fill(name);
    await page.locator(SEL.description).fill('created by the Playwright suite');
    if (type === 'resource') {
        await page.locator(SEL.vendorName).fill('pwVendor');
        await page.locator(SEL.vendorRelease).fill('1.0');
    }
    await loaderGone(page);
    const created = page.waitForResponse(r => r.request().method() === 'POST'
        && new RegExp(`/catalog/${SdcApi.collectionOf(type)}$`).test(r.url()), { timeout: 60_000 });
    await page.locator(SEL.chromeCreateButton).click();
    const asset = await api.readable(await created, name, type);
    const resp = await api.request.get(
        api.url(`/catalog/${SdcApi.collectionOf(type)}/${asset.id}/filteredDataByParams?include=metadata&include=categories`),
        { headers: api.headers });
    expect(resp.status()).toBe(200);
    return { asset, categories: (await resp.json()).metadata.categories };
}

test.describe('A new category in the create form', () => {
    test('a new resource category and sub-category can be chosen for a VF', async ({ browser, api }) => {
        const category = await createCategory(api, 'resource', categoryName('Pwvfcat'));
        const sub = await createSubCategory(api, category, categoryName('Pwvfsub'));

        await asDesigner(browser, async (page) => {
            await gotoWorkspaceTab(page, { type: 'resource', tab: 'general' });
            await expect(page.locator(`${SEL.category} optgroup[label="${category.name}"] option`)).toHaveText(sub.name);
            await page.locator(SEL.category).selectOption(`${category.name}_#_${sub.name}`);

            const { asset, categories } = await createFromForm(page, api, 'resource', 'PwNewCatVf');
            try {
                expect(categories.map((c: any) => [c.name, (c.subcategories || []).map((s: any) => s.name)]))
                    .toEqual([[category.name, [sub.name]]]);
            } finally {
                await api.deleteAsset(asset);
            }
        });
    });

    test('a new service category can be chosen for a service', async ({ browser, api }) => {
        const category = await createCategory(api, 'service', categoryName('Pwsvccat'));

        await asDesigner(browser, async (page) => {
            await gotoWorkspaceTab(page, { type: 'service', tab: 'general' });
            await page.locator(SEL.category).selectOption(category.name);

            const { asset, categories } = await createFromForm(page, api, 'service', 'PwNewCatSvc');
            try {
                expect(categories.map((c: any) => c.name)).toEqual([category.name]);
            } finally {
                await api.deleteAsset(asset);
            }
        });
    });
});
