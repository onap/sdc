/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2026 Deutsche Telekom AG
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

import { test, expect, SEL, gotoCold } from './fixtures/sdc';

const TOOLTIP = '.sdc-glossary-tooltip';

test.describe('glossary', () => {
    test('catalog tile badges show long names and explain them on hover', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/catalog');
        const badge = sdcPage.locator(`${SEL.catalogView} ui-tile [data-asset-type="VFC"]`).first();
        await badge.waitFor({ timeout: 60_000 });
        await expect(badge).toHaveText('Virtual Function Component');
        await badge.hover();
        await expect(sdcPage.locator(TOOLTIP)).toContainText('Virtual Function Component (VFC):');
        await sdcPage.mouse.move(0, 0);
        await expect(sdcPage.locator(TOOLTIP)).toHaveCount(0);
    });

    test('catalog type filter spells the types out', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/catalog');
        await sdcPage.locator('[data-tests-id="typeFilterTitle"]').waitFor({ timeout: 60_000 });
        await expect(sdcPage.getByRole('checkbox', { name: 'Virtual Function (VF)', exact: true })).toBeAttached();
    });

    test('Add VF explains itself on keyboard focus and Escape closes it', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        await sdcPage.locator('.w-sdc-dashboard-card-new').first().focus();
        await sdcPage.keyboard.press('Tab');
        await expect(sdcPage.locator('[data-tests-id="createResourceButton"]')).toBeFocused();
        await expect(sdcPage.locator(TOOLTIP)).toContainText('Virtual Function (VF):');
        await sdcPage.keyboard.press('Escape');
        await expect(sdcPage.locator(TOOLTIP)).toHaveCount(0);
    });

    test('glossary page is reachable from the top nav and filters', async ({ sdcPage }) => {
        await gotoCold(sdcPage, '/dashboard');
        await sdcPage.locator('[data-tests-id="main-menu-glossary-link"]').click();
        await expect(sdcPage.locator('[data-tests-id="glossary-page"]')).toBeVisible();
        await expect(sdcPage.locator('[data-tests-id="glossary-term-VF"]')).toContainText('Virtual Function');
        const last = sdcPage.locator('[data-tests-id="glossary-term-VSP"]');
        await last.scrollIntoViewIfNeeded();
        await expect(last).toBeInViewport();
        await sdcPage.locator('[data-tests-id="glossary-filter"]').fill('radio');
        await expect(sdcPage.locator('[data-tests-id="glossary-term-RU"]')).toBeVisible();
        await expect(sdcPage.locator('[data-tests-id="glossary-term-VF"]')).toHaveCount(0);
    });
});
