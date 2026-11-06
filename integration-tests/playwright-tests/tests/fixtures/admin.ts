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
 * Admin dashboard helpers (category management, user management) and the REST calls that seed them.
 * Category and user writes are ADMIN-only, so these calls send USER_ID jh0003 instead of SdcApi's
 * designer default.
 */

import { APIResponse, Page } from '@playwright/test';
import { expect, SdcApi, ADMIN_USER } from './sdc';

export type CategoryType = 'resource' | 'service';

export interface Category {
    uniqueId: string;
    name: string;
    subcategories?: { uniqueId: string; name: string }[];
}

export const ADMIN = {
    userManagementTab: '[data-tests-id="usermanagmenttab"]',
    categoryManagementTab: '[data-tests-id="categorymanagmenttab"]',
    typeHeader: (type: CategoryType) => `[data-tests-id="${type}categoryheader"]`,
    /** The page renders one list at a time; the item id follows the selected type. */
    categoryItem: (type: CategoryType) => `[data-tests-id="${type}category"]`,
    newCategory: '[data-tests-id="newcategory"]',
    newSubCategory: '[data-tests-id="newsubcategory"]',
    subCategoryItem: '[data-tests-id="subcategory"]',
    nameInput: '#add-category-name',
    createModalOk: '.custom-modal [data-tests-id="OK"]',
    errorModal: '.sdc-modal',
    userSearch: '[data-tests-id="searchbox"]',
    userRow: '[data-tests-id^="row_"]',
};

export const adminHeaders = { USER_ID: ADMIN_USER, 'Content-Type': 'application/json' };

/** 25 characters at most and `[\sa-zA-Z0-9+-]` only (LabelValidationPattern), so no uniqueName(). */
export function categoryName(prefix: string): string {
    const letters = Array.from({ length: 4 }, () => String.fromCharCode(97 + Math.floor(Math.random() * 26))).join('');
    return `${prefix}${Date.now().toString(36)}${letters}`;
}

async function expectStatus(resp: APIResponse, status: number, what: string): Promise<void> {
    if (resp.status() !== status) {
        expect(resp.status(), `${what} failed: ${(await resp.text()).slice(0, 1000)}`).toBe(status);
    }
}

const collection = (type: CategoryType) => `${type}s`;

export async function createCategory(api: SdcApi, type: CategoryType, name: string): Promise<Category> {
    const resp = await api.request.post(api.url(`/category/${collection(type)}`), { headers: adminHeaders, data: { name } });
    await expectStatus(resp, 201, `creating ${type} category ${name}`);
    return resp.json();
}

export async function createSubCategory(api: SdcApi, category: Category, name: string): Promise<Category> {
    const resp = await api.request.post(api.url(`/category/resources/${category.uniqueId}/subCategory`),
        { headers: adminHeaders, data: { name } });
    await expectStatus(resp, 201, `creating sub-category ${name} under ${category.name}`);
    return resp.json();
}

export async function categories(api: SdcApi, type: CategoryType): Promise<Category[]> {
    const resp = await api.request.get(api.url(`/categories/${collection(type)}`), { headers: adminHeaders });
    await expectStatus(resp, 200, `listing ${type} categories`);
    return resp.json();
}

export async function categoriesNamed(api: SdcApi, type: CategoryType, name: string): Promise<Category[]> {
    return (await categories(api, type)).filter(c => c.name.toLowerCase() === name.toLowerCase());
}

/**
 * Opens Category Management on the given type's list, from the admin dashboard the ADMIN login lands
 * on. The page lists the categories cached at app start, so one created through the API afterwards is
 * not shown until a reload; tests on this page therefore create their preconditions through the page.
 */
export async function openCategoryManagement(page: Page, type: CategoryType): Promise<void> {
    await page.locator(ADMIN.categoryManagementTab).click();
    await expect(page.locator(ADMIN.typeHeader(type)),
        'Category Management rendered its buttons without labels (blank translate pipe output; '
        + 'category-management.component.ts lacks the languageChanged re-render admin-dashboard.component.ts has)')
        .toHaveText(/\S/);
    await page.locator(ADMIN.typeHeader(type)).click();
    await expect(page.locator(ADMIN.typeHeader(type))).toHaveAttribute('aria-pressed', 'true');
    await expect(page.locator(ADMIN.categoryItem(type)).first()).toBeVisible({ timeout: 30_000 });
}

/** Fills the create-(sub)category modal and presses OK. Resolves with the POST's response. */
export async function submitCategoryModal(page: Page, name: string): Promise<APIResponse> {
    await expect(page.locator(ADMIN.nameInput)).toBeVisible();
    await page.locator(ADMIN.nameInput).fill(name);
    const post = page.waitForResponse(r => r.request().method() === 'POST' && /\/v1\/category\//.test(r.url()));
    await page.locator(ADMIN.createModalOk).click();
    return post;
}

// ---------------------------------------------------------------------------
// Users
// ---------------------------------------------------------------------------

export interface SdcUser {
    userId: string;
    firstName: string;
    lastName: string;
    email: string;
    role: string;
}

/**
 * Provisions a user through `POST /v1/user`, which the BE still serves even though the admin UI has
 * been read-only since 2020.
 */
export async function createUser(api: SdcApi, role = 'DESIGNER'): Promise<SdcUser> {
    const letters = (n: number) => Array.from({ length: n }, () => String.fromCharCode(97 + Math.floor(Math.random() * 26))).join('');
    const tag = letters(6);
    const user: SdcUser = {
        userId: `pw${tag}${letters(4)}`,
        firstName: `Pwfirst${tag}`,
        lastName: `Pwlast${tag}`,
        email: `pw.${tag}@example.org`,
        role,
    };
    const resp = await api.request.post(api.url('/user'), { headers: adminHeaders, data: user });
    await expectStatus(resp, 201, `creating user ${user.userId}`);
    return user;
}

/** `DELETE /v1/user/{id}` deactivates the user, which removes it from the user list. */
export async function deactivateUser(api: SdcApi, user: SdcUser): Promise<void> {
    await api.request.delete(api.url(`/user/${user.userId}`), { headers: adminHeaders })
        .catch(() => { /* best-effort cleanup */ });
}

/** Creates a category through the page, as a precondition. Leaves the create modal closed. */
export async function addCategoryViaUi(page: Page, type: CategoryType, name: string): Promise<void> {
    await page.locator(ADMIN.newCategory).click();
    const resp = await submitCategoryModal(page, name);
    expect(resp.status(), `creating ${type} category ${name}`).toBe(201);
    await expect(page.locator(ADMIN.categoryItem(type)).filter({ hasText: name })).toBeVisible();
}

export async function addSubCategoryViaUi(page: Page, parent: string, name: string): Promise<void> {
    await page.locator(ADMIN.categoryItem('resource')).filter({ hasText: parent }).click();
    await page.locator(ADMIN.newSubCategory).click();
    const resp = await submitCategoryModal(page, name);
    expect(resp.status(), `creating sub-category ${name}`).toBe(201);
    await expect(page.locator(ADMIN.subCategoryItem).filter({ hasText: name })).toBeVisible();
}
