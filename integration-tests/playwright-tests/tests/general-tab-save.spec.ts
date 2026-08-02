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
 * General-tab data-integrity guards.
 *
 * WHAT THIS GUARDS — three real defects, all of which were green in Jest, AOT and Selenium:
 *   1. SILENT DATA LOSS. After the WorkspaceContainerComponent migration, GeneralViewModel's
 *      $scope.save() was left as a no-op shim: editing the description and clicking Save fired
 *      NO PUT and discarded the edit without any error. Hence the assertion is on the PUT
 *      REQUEST BODY, not on the UI — a UI that still shows the typed text proves nothing.
 *   2. EMPTY DROPDOWNS. The Service Role / Service Function selects were bound to
 *      `component?.serviceRoleValues`, a property that exists nowhere, so both rendered zero
 *      options. A Service is required to reproduce: a VF never renders these service-only fields.
 *   3. Template compile errors, which stop the app bootstrapping entirely (the smoke test).
 *
 * HOW TO RUN: see README.md — `npx playwright test general-tab-save`.
 */

import { test, expect, SEL, settles, dismissTransientModal, gotoWorkspaceTab } from './fixtures/sdc';

const METADATA_PUT = /\/v1\/catalog\/(resources|services)\/[^/]+\/metadata/;

test.describe('General tab', () => {

    test('renders and settles (guards against template compile errors)', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwGenSmoke');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });

        expect(await settles(sdcPage), 'page did not settle').toBe(true);

        await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 15_000 });
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator(SEL.description)).toBeVisible({ timeout: 10_000 });
        await expect(sdcPage.locator(SEL.formSaveButton)).toBeVisible({ timeout: 10_000 });
    });

    /**
     * THE data-loss guard. Asserts on the PUT request body because the failure mode was silent:
     * the field kept showing the typed text while nothing was ever sent to the backend.
     */
    test('editing the description fires a metadata PUT carrying the edit', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwSavePut');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.description)).toBeVisible({ timeout: 15_000 });
        await dismissTransientModal(sdcPage);

        const putPromise = sdcPage.waitForRequest(
            (req) => req.method() === 'PUT' && METADATA_PUT.test(req.url()),
            { timeout: 20_000 });

        const edited = 'edited by the save-regression guard ' + Date.now();
        await sdcPage.locator(SEL.description).fill(edited);
        await sdcPage.locator(SEL.formSaveButton).click();

        const putReq = await putPromise;
        expect(putReq.url()).toMatch(METADATA_PUT);
        expect(putReq.postData() || '',
            'the PUT body does not carry the edited description — the edit was discarded')
            .toContain(edited);
    });

    /**
     * Service-only fields, which a VF never renders — hence the Service rather than a VF.
     *
     * general-tab.component.html:334 branches on getMetadataKeyValidValues('Service Role'):
     *   - non-empty  → a <select data-tests-id="roleOption"> of validValues + an 'Others' sentinel
     *   - empty      → the *ngIf-else free-text <input formControlName="serviceRole">
     * Which branch is taken depends entirely on whether the service's CATEGORY defines the
     * metadata key. On a stock 'Network Service' category it does not, so the free-text input is
     * the CORRECT render and asserting a populated dropdown here would be asserting a fiction.
     *
     * The real bug this guards is a THIRD state, which is what actually shipped: the select was
     * bound to `component?.serviceRoleValues` — a property that exists nowhere — so the *ngIf was
     * truthy-but-empty and rendered a dropdown with ZERO options and no free-text fallback. So
     * the invariant is: exactly one of the two controls renders, and if it is the select it has
     * options. Neither rendering, or an empty select, is the regression.
     */
    test('Service Role and Service Function render exactly one populated control', async ({ sdcPage, api }) => {
        const svc = await api.createService('PwSvcRoleFn');
        await gotoWorkspaceTab(sdcPage, { id: svc.id, type: 'service', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 15_000 });

        for (const field of [
            { select: 'select[data-tests-id="roleOption"]', input: 'input[data-tests-id="serviceRole"]', label: 'Service Role' },
            { select: 'select[data-tests-id="functionOption"]', input: 'input[data-tests-id="serviceFunction"]', label: 'Service Function' },
        ]) {
            const selects = await sdcPage.locator(field.select).count();
            const inputs = await sdcPage.locator(field.input).count();

            expect(selects + inputs,
                `${field.label}: expected exactly one of the dropdown or the free-text input to `
                + `render, found ${selects} select(s) and ${inputs} input(s)`).toBe(1);

            if (selects === 1) {
                const options = await sdcPage.locator(field.select).locator('option').count();
                expect(options,
                    `${field.label}: the dropdown rendered but is EMPTY — its *ngIf was truthy `
                    + `while its option source was not (the serviceRoleValues regression)`)
                    .toBeGreaterThan(0);
            }
        }
    });
});
