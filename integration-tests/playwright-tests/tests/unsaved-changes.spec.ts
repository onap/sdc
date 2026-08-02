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
 * The unsaved-changes navigation guard.
 *
 * WHAT THIS GUARDS — the single most destructive thing the ui-router → Angular Router swap
 * (phase 13 CR 2) can break, because breaking it silently loses a user's work:
 *
 *   Today the guard is a ui-router `$stateChangeStart` interception in the app.ts run block
 *   (`onStateChangeStart` :686-700 → `onNavigateOut` :619-638). CR 2 DELETES that whole run
 *   block (task 10) and replaces the mechanism with a `CanDeactivate` guard wired onto
 *   individual child routes.
 *
 *   That replacement is invisible to every other gate. `UnsavedChangesGuard` gets its own Jest
 *   unit tests, but they instantiate the guard directly — so if it is registered on the WRONG
 *   routes, or on none, the unit tests still pass, `build:prod` still passes, and Selenium still
 *   passes. Nothing but a real navigation from a real dirty form detects it.
 *
 * WHY THE GENERAL TAB and not properties-assignment: the General tab has NO modal of its own
 * (verified — no `showUnsavedChangesAlert` anywhere in general-tab.component.ts). It depends
 * entirely on the run-block guard, which makes it the tab that CR 2 is most likely to strand.
 * It is also reachable in two steps on a freshly created VF, whereas dirtying PA requires
 * composition instances.
 *
 * NOTE this exercises the SECOND of the two 'navigate-modal' spellings — see SEL.warningModal
 * in the fixture for why its selector is `div.sdc-modal` and its OK button is
 * `navigate-modal-button-ok` rather than the `'OK'` testId app.ts:626 appears to set.
 *
 * HOW TO RUN: see README.md — `npx playwright test unsaved-changes`.
 */

import { test, expect, SEL, settles, gotoWorkspaceTab, currentRoute } from './fixtures/sdc';

/**
 * Opens a freshly created VF's General tab and dirties it, leaving the page on that tab with
 * unsaved changes pending. Editing `description` is the lightest possible dirtying edit: a
 * free-text field with no validation and no cross-field consequences. The typing alone is
 * enough — general-tab.component.ts:1052-1058 subscribes to `form.valueChanges` and mirrors the
 * old $watch('editForm.$dirty') → setUnsavedChanges(true) there, so no blur is required.
 */
async function openDirtyGeneralTab(page, api): Promise<string> {
    const vf = await api.createVf('PwUnsaved');
    await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'general' });
    await settles(page);
    await expect(page.locator(SEL.name)).toBeVisible({ timeout: 30_000 });

    await page.locator(SEL.description).fill('dirtied by the Playwright suite');
    await page.locator(SEL.description).blur();
    await settles(page);

    return vf.id;
}

test.describe('Unsaved-changes guard', () => {

    /**
     * The core assertion: a dirty form must BLOCK the navigation and warn, not silently discard.
     * Both halves matter — the modal appearing is not enough if the route changed underneath it,
     * so the route is asserted to be unchanged while the modal is open.
     */
    test('navigating away from a dirty General tab warns and does not leave the page',
        async ({ sdcPage, api }) => {
            const id = await openDirtyGeneralTab(sdcPage, api);

            await sdcPage.locator(SEL.homeBreadcrumb).click();

            await expect(sdcPage.locator(SEL.warningModal),
                'no warning modal — the dirty-form guard did not fire and the edit would be lost')
                .toBeVisible({ timeout: 15_000 });
            await expect(sdcPage.locator(SEL.warningModal))
                .toContainText(/unsaved changes will be lost/i);

            // The navigation must be BLOCKED while the modal is open, not merely announced.
            expect(await currentRoute(sdcPage),
                'the route changed despite the guard — the warning is cosmetic')
                .toContain(`/workspace/${id}/resource/general`);
        });

    /**
     * The other direction: confirming must actually proceed. A guard that blocks and then never
     * releases is just as broken as one that never fires, and it is the easy failure mode when
     * `CanDeactivate` returns an Observable that does not complete.
     */
    test('confirming the warning proceeds to the target route', async ({ sdcPage, api }) => {
        await openDirtyGeneralTab(sdcPage, api);

        await sdcPage.locator(SEL.homeBreadcrumb).click();
        await expect(sdcPage.locator(SEL.warningModal)).toBeVisible({ timeout: 15_000 });

        await sdcPage.locator(SEL.warningModalOkButton).click();
        await settles(sdcPage);

        await expect.poll(() => currentRoute(sdcPage), { timeout: 20_000 }).toMatch(/^\/dashboard$/);
        await expect(sdcPage.locator(SEL.homeView)).toBeAttached({ timeout: 20_000 });
    });

    /**
     * The control. Without this, both tests above would still pass if the modal were shown on
     * EVERY navigation regardless of dirtiness — a guard stuck open is a real and annoying
     * failure mode, and it is what a `hasChangedData` check inverted by mistake produces.
     */
    test('a CLEAN General tab navigates away with no warning', async ({ sdcPage, api }) => {
        const vf = await api.createVf('PwClean');
        await gotoWorkspaceTab(sdcPage, { id: vf.id, type: 'resource', tab: 'general' });
        await settles(sdcPage);
        await expect(sdcPage.locator(SEL.name)).toBeVisible({ timeout: 30_000 });

        await sdcPage.locator(SEL.homeBreadcrumb).click();
        await settles(sdcPage);

        await expect(sdcPage.locator(SEL.warningModal),
            'a clean form warned about unsaved changes — the guard fires unconditionally')
            .toHaveCount(0);
        await expect.poll(() => currentRoute(sdcPage), { timeout: 20_000 }).toMatch(/^\/dashboard$/);
    });
});
