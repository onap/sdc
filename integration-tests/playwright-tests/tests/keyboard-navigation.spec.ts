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

import { test, expect, SEL, ADMIN_USER, gotoCold, gotoWorkspaceTab, currentRoute } from './fixtures/sdc';

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

    test('dashboard filters come before the cards in the tab order', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        const filter = sdcPage.locator('.w-sdc-left-sidebar input.sdc-checkbox__input').first();
        await filter.waitFor({ state: 'attached', timeout: 60_000 });
        await filter.focus();
        await sdcPage.keyboard.press('Tab');
        await sdcPage.keyboard.press('Tab');
        await sdcPage.keyboard.press('Tab');
        await sdcPage.keyboard.press('Tab');
        await expect(sdcPage.locator('#dashboard-main-scroll')).toBeFocused();
    });

    test('closing Import Type puts focus back on the Import card', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        await sdcPage.locator('.w-sdc-dashboard-card-new[aria-label="Import"]').focus();
        const importType = sdcPage.locator('[data-tests-id="importTypebutton"]');
        await importType.focus();
        await sdcPage.keyboard.press('Enter');
        const dialog = sdcPage.getByRole('dialog', { name: 'Import Type' });
        await expect(dialog).toBeVisible();
        await sdcPage.keyboard.press('Escape');
        await expect(dialog).toBeHidden();
        await expect(sdcPage.locator('.w-sdc-dashboard-card-new[aria-label="Import"]')).toBeFocused();
    });

    test('catalog scrollbar thumbs are not tab stops', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/catalog');
        const thumbs = sdcPage.locator('.sdc-catalog-body-container .ps-scrollbar-x, .sdc-catalog-body-container .ps-scrollbar-y');
        await expect(thumbs).toHaveCount(2, { timeout: 60_000 });
        for (const thumb of await thumbs.all()) {
            await expect(thumb).toHaveAttribute('tabindex', '-1');
        }
    });

    test('a new service is announced by its heading before it has a name', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        await gotoWorkspaceTab(sdcPage, { type: 'service' });
        await expect(sdcPage.getByRole('heading', { level: 1 })).toHaveText('Create new service');
    });

    test('an sdc-modal takes focus, keeps it, closes on Escape and gives it back', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        const card = sdcPage.locator('.w-sdc-dashboard-card-new[aria-label="Import"]');
        await card.focus();
        await sdcPage.getByRole('button', { name: 'Import VSP' }).focus();
        await sdcPage.keyboard.press('Enter');
        const dialog = sdcPage.getByRole('dialog', { name: 'Import Vendor Software Product (VSP)' });
        await expect(dialog).toBeVisible();
        await expect(dialog).toHaveAttribute('aria-modal', 'true');
        await expect.poll(() => dialog.evaluate((d) => d.contains(document.activeElement))).toBe(true);
        for (let i = 0; i < 25; i++) {
            await sdcPage.keyboard.press('Tab');
            expect(await dialog.evaluate((d) => d.contains(document.activeElement)), `Tab ${i + 1} left the modal`).toBe(true);
        }
        await sdcPage.keyboard.press('Shift+Tab');
        expect(await dialog.evaluate((d) => d.contains(document.activeElement))).toBe(true);
        await sdcPage.keyboard.press('Escape');
        await expect(dialog).toBeHidden();
        await expect(card).toBeFocused();
    });

    test('a focused sdc-checkbox shows a focus ring on its box', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        const box = sdcPage.locator('.w-sdc-left-sidebar input.sdc-checkbox__input').first();
        await box.waitFor({ state: 'attached', timeout: 60_000 });
        await box.focus();
        await sdcPage.keyboard.press('Shift+Tab');
        await sdcPage.keyboard.press('Tab');
        await expect(box).toBeFocused();
        expect(await box.evaluate((input) => getComputedStyle(input.nextElementSibling, '::before').outlineStyle)).toBe('solid');
    });

    test('a collapsed accordion takes its controls out of the tab order', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/onboardVendor');
        const header = sdcPage.locator('button.sdc-accordion-header', { hasText: 'ENTITY TYPE' });
        await expect(header).toHaveAttribute('aria-expanded', 'true', { timeout: 60_000 });
        await header.focus();
        await sdcPage.keyboard.press('Enter');
        await expect(header).toHaveAttribute('aria-expanded', 'false');
        // The body only turns hidden once its fade-out has finished.
        await expect(sdcPage.locator(`#${await header.getAttribute('aria-controls')}`)).toHaveCSS('visibility', 'hidden');
        await sdcPage.keyboard.press('Tab');
        await expect(sdcPage.locator('button.sdc-accordion-header', { hasText: 'PERMISSIONS' })).toBeFocused();
    });

    test.describe('as admin', () => {
        test.use({ sdcUser: ADMIN_USER });

        test('Monitor is a single link', async ({ sdcPage }) => {
            await gotoCold(sdcPage, '/adminDashboard');
            const monitor = sdcPage.locator('[data-tests-id="monitor"]');
            await expect(monitor).toHaveJSProperty('tagName', 'A', { timeout: 60_000 });
            await expect(monitor.locator('a, button')).toHaveCount(0);
            await sdcPage.locator('[data-tests-id="categorymanagmenttab"]').focus();
            await sdcPage.keyboard.press('Tab');
            await expect(monitor).toBeFocused();
        });
    });
});
