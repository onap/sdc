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
 * User Management search on the admin dashboard. The page is read-only, so the users it finds are
 * provisioned through the REST API.
 */

import { Page } from '@playwright/test';
import { test, expect, ADMIN_USER } from './fixtures/sdc';
import { ADMIN, SdcUser, createUser, deactivateUser } from './fixtures/admin';

/**
 * From the admin dashboard the ADMIN login lands on (see openCategoryManagement for why not cold).
 * The login already rendered User Management, possibly before the user was seeded; switching tabs
 * remounts it, which refetches the user list.
 */
async function search(page: Page, term: string): Promise<void> {
    await page.locator(ADMIN.categoryManagementTab).click();
    await page.locator(ADMIN.userManagementTab).click();
    await expect(page.locator(ADMIN.userRow).first()).toBeVisible({ timeout: 30_000 });
    await page.locator(ADMIN.userSearch).fill(term);
}

/** The cells of the row whose userId cell is `userId`, keyed by column. */
async function rowOf(page: Page, userId: string) {
    const row = page.locator(ADMIN.userRow).filter({
        has: page.locator(`[data-tests-id^="userId_"]`, { hasText: new RegExp(`^\\s*${userId}\\s*$`) }),
    });
    await expect(row).toHaveCount(1);
    return row;
}

test.describe('User management search', () => {
    test.use({ sdcUser: ADMIN_USER });

    let user: SdcUser;
    test.beforeEach(async ({ api }) => { user = await createUser(api); });
    test.afterEach(async ({ api }) => { await deactivateUser(api, user); });

    for (const criterion of ['userId', 'firstName', 'lastName', 'email'] as const) {
        test(`finds a user by ${criterion}`, async ({ sdcPage }) => {
            await search(sdcPage, user[criterion]);

            await expect(sdcPage.locator(ADMIN.userRow)).toHaveCount(1);
            const row = await rowOf(sdcPage, user.userId);
            await expect(row.locator('[data-tests-id^="firstName_"]')).toHaveText(user.firstName);
            await expect(row.locator('[data-tests-id^="lastName_"]')).toHaveText(user.lastName);
            await expect(row.locator('[data-tests-id^="email_"]')).toHaveText(user.email);
            await expect(row.locator('[data-tests-id^="role_"]')).toHaveText('designer');
        });
    }

    test('finds users by role', async ({ sdcPage }) => {
        await search(sdcPage, 'designer');

        await rowOf(sdcPage, user.userId);
        await expect(sdcPage.locator('[data-tests-id^="role_"]').filter({ hasNotText: 'designer' })).toHaveCount(0);
        await expect(sdcPage.locator('[data-tests-id^="userId_"]').filter({ hasText: ADMIN_USER })).toHaveCount(0);
    });
});
