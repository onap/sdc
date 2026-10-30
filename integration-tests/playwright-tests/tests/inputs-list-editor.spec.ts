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
 * List inputs on the Inputs tab are edited entry by entry.
 *
 * WHAT THIS GUARDS: the Value cell of a list input used to be a plain text box whose text was sent
 * verbatim, so typing a value the way the Properties tab suggests (`Huawei/cellradius.jinja`) was
 * rejected with SVC4726 "Value provided in invalid format". The assertions are on the stored default,
 * because a UI that shows the entries proves nothing about what was saved.
 *
 * HOW TO RUN: see README.md — `npx playwright test inputs-list-editor`.
 */

import { test, expect, settles, gotoWorkspaceTab } from './fixtures/sdc';

const UPDATE_INPUTS = /\/v1\/catalog\/resources\/[^/]+\/update\/inputs/;

test.describe('Inputs tab list editor', () => {

    test('adds, deletes and saves list entries as a JSON list', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwListInput');
        const inputName = await api.addListInput(vf);
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'properties_assignment' });
        await settles(sdcPage);
        await sdcPage.locator('[data-tests-id="Inputs"][role="tab"]').click();

        const row = sdcPage.locator('.table-row')
            .filter({ has: sdcPage.getByRole('button', { name: inputName, exact: true }) });
        const entries = row.locator('.valueCol input[type="text"]');
        const addEntry = row.locator(`[data-tests-id="add-to-list-${inputName}"]`);
        const save = sdcPage.locator('[data-tests-id="properties-save-button"]');

        await expect(addEntry).toBeVisible({ timeout: 30_000 });
        await expect(row.locator(`[data-tests-id="value-input-${inputName}"]`),
            'the list input still renders the plain text box').toHaveCount(0);

        await addEntry.click();
        await entries.nth(0).fill('sg-a');
        await addEntry.click();
        await entries.nth(1).fill('sg-b');
        await row.locator('[data-tests-id^="delete-from-list-"]').first().click();
        await expect(entries).toHaveCount(1);
        await expect(entries.first()).toHaveValue('sg-b');

        const saved = sdcPage.waitForResponse(
            (resp) => resp.request().method() === 'POST' && UPDATE_INPUTS.test(resp.url()), { timeout: 20_000 });
        await save.click();
        expect((await saved).status(), 'saving the list input was rejected').toBe(200);

        expect(JSON.parse(await api.inputDefaultValue(vf, inputName) || 'null')).toEqual(['sg-b']);
    });
});
