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

import { test, expect, gotoCold, currentRoute } from './fixtures/sdc';

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
});
