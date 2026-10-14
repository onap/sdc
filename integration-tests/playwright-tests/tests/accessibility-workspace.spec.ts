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
 * Accessibility ratchet over the workspace tabs of a freshly created VF and Service.
 * CREATES DATA (one asset per test): never point it at a shared cluster. accessibility.spec.ts is the
 * read-only half. KNOWN semantics as there: see expectAxeRatchet().
 */
import {
    test, SEL, gotoWorkspaceTab, expectAxeRatchet, loaderGone, trackNetworkQuiet, CreatedAsset,
} from './fixtures/sdc';
import { Page } from '@playwright/test';

interface TabScan {
    tab: string;
    /** Content only the loaded tab renders, so axe never scans the empty shell or a spinner. */
    ready: string;
    known: string[];
    seed?: 'compute';
}

const COMPOSITION_READY = '[data-tests-id^="leftPalette.category."]';
const COMPOSITION_KNOWN = ['aria-command-name', 'aria-tab-name', 'label'];

const VF_TABS: TabScan[] = [
    { tab: 'general', ready: SEL.name, known: [] },
    { tab: 'deployment_artifacts', ready: 'deployment-artifact-page ngx-datatable', known: [] },
    { tab: 'information_artifacts', ready: 'information-artifact-page ngx-datatable', known: [] },
    { tab: 'tosca_artifacts', ready: 'tosca-artifact-page datatable-body-row', known: [] },
    { tab: 'properties_assignment', ready: '[data-tests-id="Inputs"]', known: [] },
    { tab: 'composition', ready: COMPOSITION_READY, known: COMPOSITION_KNOWN },
    { tab: 'activity_log', ready: 'activity-log ngx-datatable', known: [] },
    { tab: 'req_and_capabilities', ready: 'req-and-capabilities datatable-body-row', seed: 'compute', known: [] },
    { tab: 'attributes_outputs', ready: '[data-tests-id="Outputs"]', known: [] },
    { tab: 'deployment', ready: 'deployment-page #sdc-tab-0', known: ['aria-tab-name'] },
    { tab: 'interface_operation', ready: '.workspace-interface-operation', known: [] },
];

const SERVICE_TABS: TabScan[] = [
    { tab: 'general', ready: SEL.name, known: [] },
    { tab: 'composition', ready: COMPOSITION_READY, known: COMPOSITION_KNOWN },
    { tab: 'properties_assignment', ready: '[data-tests-id="Inputs"]', known: ['label', 'select-name'] },
    { tab: 'activity_log', ready: 'activity-log ngx-datatable', known: [] },
    { tab: 'tosca_artifacts', ready: 'tosca-artifact-page datatable-body-row', known: [] },
    { tab: 'management_workflow', ready: '.flow-editor-punch-out-host > *', known: ['region'] },
];

async function openTab(page: Page, asset: CreatedAsset, scan: TabScan): Promise<void> {
    const quiet = trackNetworkQuiet(page);
    try {
        await gotoWorkspaceTab(page, { id: asset.id, type: asset.type, tab: scan.tab });
        await page.locator(scan.ready).first().waitFor({ state: 'attached', timeout: 30_000 });
        // A tab's <loader> shows only after a 500 ms delay, so loaderGone() alone can pass before it
        // appears. Once the tab's requests have finished it is gone for good.
        await quiet.settled();
        await loaderGone(page);
    } finally {
        quiet.dispose();
    }
}

test.describe('accessibility ratchet: VF workspace', () => {
    for (const scan of VF_TABS) {
        test(`vf ${scan.tab}: no axe violations beyond the known list`, async ({ sdcPage, api }) => {
            const vf = await api.createVf('PwAxeVf');
            if (scan.seed === 'compute') {
                await api.addComputeInstance(vf);
            }
            await openTab(sdcPage, vf, scan);
            await expectAxeRatchet(sdcPage, scan.known);
        });
    }
});

test.describe('accessibility ratchet: Service workspace', () => {
    for (const scan of SERVICE_TABS) {
        test(`service ${scan.tab}: no axe violations beyond the known list`, async ({ sdcPage, api }) => {
            const svc = await api.createService('PwAxeSvc');
            await openTab(sdcPage, svc, scan);
            await expectAxeRatchet(sdcPage, scan.known);
        });
    }
});
