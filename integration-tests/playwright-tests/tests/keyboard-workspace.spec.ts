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
 * Keyboard operation of the asset workspace: the top bar, catalog-ui's own <tabs> and the
 * sortable ngx-datatable headers.
 *
 * Every test creates a VF, so this file runs on the CI stack only, never against tnaplab.
 *
 * HOW TO RUN: see README.md — `npx playwright test keyboard-workspace`.
 */

import { test, expect, SEL, settles, currentRoute, gotoWorkspaceTab } from './fixtures/sdc';

test.describe('Workspace keyboard operation', () => {

    test('the version select is named and Close works from the keyboard', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwKbdClose');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        expect(await settles(sdcPage), 'page did not settle').toBe(true);

        await expect(sdcPage.locator(SEL.versionHeader)).toHaveAccessibleName('Version', { timeout: 15_000 });

        const close = sdcPage.locator('.sdc-workspace-top-bar-buttons > button.x-btn');
        await expect(close).toHaveAttribute('type', 'button');
        await expect(close).toHaveAccessibleName('Close');
        await close.focus();
        await expect(close).toBeFocused();
        await sdcPage.keyboard.press('Enter');
        await settles(sdcPage);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 15_000 }).not.toContain('/workspace');
    });

    test('properties assignment tabs: arrows move focus, Enter selects', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwKbdTabs');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'properties_assignment' });
        expect(await settles(sdcPage), 'page did not settle').toBe(true);

        const properties = sdcPage.locator('[data-tests-id="Properties"][role="tab"]');
        const inputs = sdcPage.locator('[data-tests-id="Inputs"][role="tab"]');
        await expect(properties).toHaveAttribute('aria-selected', 'true', { timeout: 15_000 });

        await properties.focus();
        await sdcPage.keyboard.press('ArrowRight');
        await expect(inputs).toBeFocused();
        await expect(inputs).toHaveAttribute('aria-selected', 'false');
        await expect(properties).toHaveAttribute('aria-selected', 'true');

        await sdcPage.keyboard.press('Enter');
        await expect(inputs).toHaveAttribute('aria-selected', 'true');
        await expect(inputs).toHaveClass(/\bactive\b/);
        await expect(inputs).toHaveAttribute('tabindex', '0');
        await expect(properties).toHaveAttribute('tabindex', '-1');
    });

    test('activity log: Enter on a header label sorts the table', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwKbdSort');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'activity_log' });
        expect(await settles(sdcPage), 'page did not settle').toBe(true);

        const timeCell = sdcPage.locator('ngx-datatable .datatable-header-cell.sortable', { hasText: 'Time' });
        const time = timeCell.locator('.datatable-header-cell-label');
        await expect(time).toHaveAttribute('role', 'button', { timeout: 15_000 });
        await expect(time).toHaveAttribute('tabindex', '0');
        await expect(timeCell).not.toHaveClass(/\bsort-active\b/);

        await time.focus();
        await sdcPage.keyboard.press('Enter');
        await expect(timeCell).toHaveClass(/\bsort-active\b/);
        await expect(time).toHaveAccessibleName('Time, sorted ascending');
        await expect(time).toBeFocused();

        await sdcPage.keyboard.press(' ');
        await expect(time).toHaveAccessibleName('Time, sorted descending');
    });
});
