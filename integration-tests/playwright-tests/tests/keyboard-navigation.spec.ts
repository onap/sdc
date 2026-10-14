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

import { test, expect, SEL, gotoCold, currentRoute } from './fixtures/sdc';

test.describe('keyboard navigation', () => {
    for (const route of ['/catalog', '/dashboard', '/onboardVendor']) {
        test(`skip link on ${route} moves focus into main and keeps the route`, async ({ sdcPage }) => {
            await gotoCold(sdcPage, route);
            // gotoCold() can return while the splash still fills <app-root>; a Tab pressed then
            // finds nothing focusable and the skip link is never reached.
            await sdcPage.locator('#main-content').waitFor({ state: 'attached', timeout: 60_000 });
            await sdcPage.keyboard.press('Tab');
            await expect(sdcPage.locator('.skip-to-main')).toBeFocused();
            await sdcPage.keyboard.press('Enter');
            expect(await currentRoute(sdcPage)).toBe(route);
            await expect(sdcPage.locator('#main-content')).toBeFocused();
        });
    }

    test('top-nav menu is reachable and operable by keyboard', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        const catalog = sdcPage.locator(SEL.catalogButton);
        await catalog.focus();
        await expect(catalog).toBeFocused();
        await sdcPage.keyboard.press('Enter');
        await expect.poll(() => currentRoute(sdcPage)).toBe('/catalog');
        await expect(sdcPage.locator(SEL.catalogView)).toBeAttached();
    });

    test('dashboard ADD card reveals its buttons on focus and keeps them while tabbing in', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        await sdcPage.locator('.w-sdc-dashboard-card-new').first().focus();
        const add = sdcPage.locator('[data-tests-id="createServiceButton"]');
        await expect(add).toBeVisible();
        await sdcPage.keyboard.press('Tab');
        await expect(sdcPage.locator('[data-tests-id="createResourceButton"]')).toBeFocused();
        await expect(add).toBeVisible();
    });

    test('catalog filter sections toggle with the keyboard', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/catalog');
        const title = sdcPage.locator('.i-sdc-designer-leftbar-section-title').first();
        await title.waitFor({ timeout: 60_000 });
        const before = await title.getAttribute('aria-expanded');
        await title.focus();
        await sdcPage.keyboard.press('Enter');
        await expect(title).not.toHaveAttribute('aria-expanded', before as string);
    });
});
