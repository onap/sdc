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
 * Playwright regression guard for the General-tab Phase-3 Angular migration.
 *
 * The bug this spec guards against (Phase-2 regression, fixed in Phase 3):
 *   GeneralViewModel was an AngularJS controller whose $scope.save() was a no-op shim
 *   after the WorkspaceContainerComponent migration.  Editing the description field and
 *   clicking Save silently discarded the change — NO PUT was fired.
 *
 * What this spec asserts:
 *   1.  The General tab renders and the Angular app settles (regression: if the template
 *       has a broken pipe / compile error the app never bootstraps → all tests fail).
 *   2.  Editing the description and clicking the General-tab Save button fires a PUT to
 *       /sdc1/feProxy/rest/v1/catalog/(resources|services)/{uniqueId}/metadata carrying
 *       the edited description in its request body.
 *
 * Environment notes:
 *   - Requires the webpack dev-server running at SDC_BASE_URL (default :8285).
 *     The dev-server must have been built from the general-tab-angular-component branch.
 *   - The backend must be reachable (Docker stack or tnaplab).
 *   - Login uses the webseal-simulator credentials cs0008 / 123123a.
 *   - In CI the Maven profile sets SDC_BASE_URL to the Docker FE container.
 */

import { test, expect, Request } from '@playwright/test';

// ---------------------------------------------------------------------------
// Shared helpers (mirrors workspace-shell.spec.ts patterns)
// ---------------------------------------------------------------------------

/** pageLoadWait logic: mirrors the exact check Selenium / Protractor uses.  */
const PAGE_LOAD_WAIT = `
  try {
    if (document.readyState !== 'complete') return false;
    if (window.jQuery && window.jQuery.active) return false;
    if (window.angular) {
      if (!window.qa) { window.qa = { doneRendering: false }; }
      var injector = window.angular.element('body').injector();
      if (!injector) return false;
      var $rootScope = injector.get('$rootScope');
      var $http = injector.get('$http');
      var $timeout = injector.get('$timeout');
      if ($rootScope.$$phase === '$apply' || $rootScope.$$phase === '$digest' || $http.pendingRequests.length !== 0) {
        window.qa.doneRendering = false; return false;
      }
      if (!window.qa.doneRendering) { $timeout(function() { window.qa.doneRendering = true; }, 0); return false; }
    }
    return true;
  } catch (ex) { return false; }
`;

async function login(page: any): Promise<void> {
    await page.goto('/login');
    await page.locator('input[name="userId"]').fill('cs0008');
    await page.locator('input[name="password"]').fill('123123a');
    await page.locator('input[value="Login"]').click();
    await page.waitForURL('**/sdc1**', { timeout: 30_000 });
    await page.waitForTimeout(5_000);
}

async function settles(page: any): Promise<boolean> {
    const start = Date.now();
    while (Date.now() - start < 30_000) {
        const r = await page.evaluate(new Function(PAGE_LOAD_WAIT) as any).catch(() => 'FROZEN');
        if (r === 'FROZEN') return false;
        if (r === true) return true;
        await page.waitForTimeout(500);
    }
    return false;
}

// ---------------------------------------------------------------------------
// Test 1: General tab render & settle (smoke — guards against compile errors)
// ---------------------------------------------------------------------------

test('General tab: renders and settles after Angular migration (smoke)', async ({ page }) => {
    await login(page);

    // Create a fresh VF so we always have an asset to open in EDIT mode.
    const created = await page.evaluate(() => {
        const $http = (window as any).angular.element('body').injector().get('$http');
        const name = 'PwGeneralTabSmoke' + Date.now();
        return $http.post('/sdc1/feProxy/rest/v1/catalog/resources', {
            name,
            description: 'created by general-tab smoke test',
            componentType: 'RESOURCE',
            resourceType: 'VF',
            categories: [{
                name: 'Generic',
                normalizedName: 'generic',
                uniqueId: 'resourceNewCategory.generic',
                subcategories: [{ name: 'Abstract', normalizedName: 'abstract', uniqueId: 'resourceNewCategory.generic.abstract' }]
            }],
            vendorName: 'pwVendor',
            vendorRelease: '1.0',
            contactId: 'cs0008',
            icon: 'defaulticon',
            tags: [name]
        }).then(
            (r: any) => ({ id: r.data.uniqueId }),
            (e: any) => ({ error: e.data || e.status })
        );
    });

    expect(created.id, `Asset creation failed: ${JSON.stringify(created)}`).toBeTruthy();

    // Navigate to workspace.general (EDIT mode — asset is NOT_CERTIFIED_CHECKOUT after create).
    await page.evaluate((id: string) => {
        (window as any).angular.element('body').injector().get('$state')
            .go('workspace.general', { id, type: 'resource', previousState: 'dashboard' });
    }, created.id);

    expect(await settles(page)).toBe(true);

    // Assert the General tab rendered (data-tests-id guards from the template).
    await expect(page.locator('[data-tests-id="GeneralLeftSideMenu"]')).toBeVisible({ timeout: 10_000 });
    await expect(page.locator('[data-tests-id="name"]')).toBeVisible({ timeout: 5_000 });
    await expect(page.locator('[data-tests-id="description"]')).toBeVisible({ timeout: 5_000 });
    // The Angular Save button (data-tests-id from GeneralTabComponent template, Task 8).
    await expect(page.locator('.w-sdc-main-container-body-content-action-buttons [data-tests-id="create/save"]')).toBeVisible({ timeout: 5_000 });

    console.log('✓ General tab rendered and settled — template has no compile errors');
});

// ---------------------------------------------------------------------------
// Test 2: Save → PUT regression guard (THE key guard for Phase-3 data-loss fix)
// ---------------------------------------------------------------------------

test('General tab save: editing description fires PUT /catalog/resources|services/.../metadata', async ({ page }) => {
    await login(page);

    // Create a fresh VF to edit (ensures we always have a checked-out asset).
    const created = await page.evaluate(() => {
        const $http = (window as any).angular.element('body').injector().get('$http');
        const name = 'PwSavePUT' + Date.now();
        return $http.post('/sdc1/feProxy/rest/v1/catalog/resources', {
            name,
            description: 'original description',
            componentType: 'RESOURCE',
            resourceType: 'VF',
            categories: [{
                name: 'Generic',
                normalizedName: 'generic',
                uniqueId: 'resourceNewCategory.generic',
                subcategories: [{ name: 'Abstract', normalizedName: 'abstract', uniqueId: 'resourceNewCategory.generic.abstract' }]
            }],
            vendorName: 'pwVendor',
            vendorRelease: '1.0',
            contactId: 'cs0008',
            icon: 'defaulticon',
            tags: [name]
        }).then(
            (r: any) => ({ id: r.data.uniqueId, name }),
            (e: any) => ({ error: e.data || e.status })
        );
    });

    expect(created.id, `Asset creation failed: ${JSON.stringify(created)}`).toBeTruthy();

    // Navigate to workspace.general in EDIT mode.
    await page.evaluate((id: string) => {
        (window as any).angular.element('body').injector().get('$state')
            .go('workspace.general', { id, type: 'resource', previousState: 'dashboard' });
    }, created.id);

    expect(await settles(page)).toBe(true);
    await expect(page.locator('[data-tests-id="description"]')).toBeVisible({ timeout: 10_000 });

    // Dismiss any transient dev-server modal (e.g. plugins-config Not Found) that might block clicks.
    const okBtn = page.locator("[data-tests-id='OK'], button:has-text('OK')");
    if (await okBtn.count()) {
        await okBtn.first().click({ timeout: 3_000 }).catch(() => {});
        await page.waitForTimeout(500);
    }

    // Arm the request interceptor BEFORE we click Save.
    // We capture any PUT to .../catalog/resources/.../metadata (non-VSP VF path from
    // ComponentService.updateResourceMetadata) or .../catalog/services/.../metadata.
    // The pattern covers both dev-server proxy (/sdc1/feProxy/rest/v1/catalog/...) and
    // direct-backend URLs (/sdc2/rest/v1/catalog/...) used in the CI Docker stack.
    let capturedPutRequest: Request | null = null;
    page.on('request', (req) => {
        if (
            req.method() === 'PUT' &&
            /\/v1\/catalog\/(resources|services)\/[^/]+\/metadata/.test(req.url())
        ) {
            capturedPutRequest = req;
        }
    });

    // Also use waitForRequest so the test fails fast if no PUT fires within the timeout.
    const putPromise = page.waitForRequest(
        (req) =>
            req.method() === 'PUT' &&
            /\/v1\/catalog\/(resources|services)\/[^/]+\/metadata/.test(req.url()),
        { timeout: 20_000 }
    );

    // Edit the description.
    const editedDescription = 'edited by playwright PUT-regression test ' + Date.now();
    await page.locator('[data-tests-id="description"]').fill(editedDescription);

    // Click the Angular Save button (rendered by GeneralTabComponent, Task 8 template).
    // Selector uses the action-buttons wrapper class to disambiguate from the shell's Create button.
    await page.locator('.w-sdc-main-container-body-content-action-buttons [data-tests-id="create/save"]').click();

    // Await the PUT.
    const putReq = await putPromise;

    // Assert 1: a PUT fired.
    expect(putReq.method()).toBe('PUT');

    // Assert 2: the PUT URL matches the expected catalog path.
    expect(putReq.url()).toMatch(/\/v1\/catalog\/(resources|services)\/[^/]+\/metadata/);

    // Assert 3: the request body contains the edited description.
    // This is the regression guard: before the fix, save() was a no-op shim and no PUT
    // was fired at all (the description silently disappeared).
    const body = putReq.postData() || '';
    expect(body).toContain(editedDescription);

    console.log(`✓ Save→PUT regression guard: PUT fired to ${putReq.url()}`);
    console.log(`  Body contained edited description: true`);

    // Optionally verify the response was 200 (not required for the regression guard but
    // nice to confirm the backend accepted the edit).
    const putResp = await putReq.response().catch(() => null);
    if (putResp) {
        console.log(`  Response status: ${putResp.status()}`);
    }
});
