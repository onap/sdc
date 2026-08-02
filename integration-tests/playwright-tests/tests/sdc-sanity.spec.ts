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
 * Login + app-bootstrap smoke tests.
 *
 * WHAT THIS GUARDS: the app bootstraps at all. Every other spec in this suite depends on
 * login working, so when this file fails, treat all other failures as cascade noise and fix
 * this first.
 *
 * HOW TO RUN: see README.md — `npx playwright test sdc-sanity`.
 */

import { test, expect, SEL, login, IS_DEV_SERVER } from './fixtures/sdc';

test.describe('SDC sanity', () => {

    /**
     * The real webseal-simulator login page renders a table of preconfigured users (cs0008,
     * jh0003, …). The dev server hand-rolls its own minimal /login — form fields only, no table —
     * so this assertion is genuinely inapplicable there rather than failing for an interesting
     * reason. Skipped rather than loosened, because the table IS the thing being checked: a
     * simulator that came up without its user list is a broken stack, and weakening the assertion
     * to "an h1 exists" would stop detecting that.
     */
    test('the simulator login page renders its user quick-links table', async ({ page }) => {
        test.skip(IS_DEV_SERVER, 'the dev server serves its own login page, which has no user table');
        await page.goto('/login');

        await expect(page.locator('h1')).toContainText(/Webseal simulator|SDC/i);

        const table = page.locator('table');
        await expect(table).toBeVisible();
        await expect(table.locator('tr')).not.toHaveCount(0);
    });

    test('login reaches the SDC home page with the top-nav rendered', async ({ page }) => {
        await login(page);

        await expect(page).toHaveTitle(/SDC|STARTER/i, { timeout: 30_000 });
        await expect(page.locator(SEL.homeButton)).toBeVisible();
        await expect(page.locator(SEL.catalogButton)).toBeVisible();
    });

    /**
     * The '#!' hash prefix is a contract, not cosmetics: every bookmark, all 70 Cypress URL
     * lines and the Selenium URL builders encode it. AngularJS supplies it today; after CR 2
     * SdcHashLocationStrategy must supply it instead. A bare '#/' here means that strategy is
     * missing or misconfigured, which silently breaks every deep link in the product.
     */
    test('the landing URL carries the #! hash prefix', async ({ sdcPage }) => {
        await expect.poll(() => sdcPage.url(), { timeout: 15_000 }).toMatch(/#!\//);
    });
});
