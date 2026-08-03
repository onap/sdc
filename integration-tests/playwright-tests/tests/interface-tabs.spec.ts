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
 * Interface Operation / Interface Definition tab guards.
 *
 * WHAT THIS GUARDS: both tabs render their migrated Angular components AND receive their
 * `component` input. Phase 13 CR 2 deleted the AngularJS shim controller that supplied
 * `component` and `readonly` to these two routes, and BOTH page components dereference
 * `this.component` synchronously inside ngOnInit's forkJoin — so a missing feed throws during
 * activation and the tab renders empty. The `.workspace-interface-*` wrapper assertions catch
 * that: the element attaches even when the component throws, but the wrapper div does not.
 *
 * NOTE ON URL SHAPES: the two tabs use inconsistent URL segments — 'interface_operation'
 * (snake_case) but 'interfaceDefinition' (camelCase) — inherited from the ui-router state names
 * 'workspace.interface_operation' and 'workspace.interface-definition'. app.routes.ts preserves
 * both verbatim; that inconsistency is a contract, not a bug to tidy up here.
 *
 * HOW TO RUN: see README.md — `npx playwright test interface-tabs`.
 */

import { test, expect, SEL, settles, currentRoute, gotoWorkspaceTab } from './fixtures/sdc';

const TABS = [
    { urlSegment: 'interface_operation', element: 'interface-operation', wrapper: '.workspace-interface-operation' },
    { urlSegment: 'interfaceDefinition', element: 'interface-definition', wrapper: '.workspace-interface-definition' },
] as const;

test.describe('Interface tabs', () => {

    for (const tab of TABS) {
        test(`${tab.urlSegment} renders its Angular component with the component input fed`, async ({ sdcPage, api }) => {
            const svc = await api.createService('PwIface');

            // Enter through the General tab first, as a user does, then switch: this exercises the
            // in-app child-route transition rather than only a cold load.
            await gotoWorkspaceTab(sdcPage, { id: svc.id, type: 'service', tab: 'general' });
            await expect(sdcPage.locator(SEL.generalSideMenu)).toBeVisible({ timeout: 30_000 });

            await gotoWorkspaceTab(sdcPage, { id: svc.id, type: 'service', tab: tab.urlSegment });
            expect(await settles(sdcPage), 'page did not settle on the interface tab').toBe(true);

            await expect(sdcPage.locator(tab.element)).toBeAttached({ timeout: 20_000 });
            // The wrapper is the discriminator: it is absent when the component throws during
            // activation because its `component` input was never supplied.
            await expect(sdcPage.locator(tab.wrapper)).toBeAttached({ timeout: 10_000 });

            expect(await currentRoute(sdcPage)).toContain(tab.urlSegment);
        });
    }
});
