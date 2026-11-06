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
 * Generic Artifact Browser (GAB): a deployment artifact added through the Create Artifact form opens,
 * from its magnifier icon, as a table built from the paths configured under `gab` in the FE's UI
 * configuration. CREATES DATA (a PNF or VF per test).
 */
import { Page } from '@playwright/test';
import { test, expect, gotoWorkspaceTab, loaderGone, CreatedAsset, SdcApi } from './fixtures/sdc';
import { fixtureFile } from './fixtures/csar';
import { componentData, createPnf } from './fixtures/onboarding';

const ART = {
    add: '[data-tests-id="add-deployment-artifact-button"]',
    file: 'sdc-modal input[type="file"][data-tests-id="fileundefined"]',
    label: 'sdc-modal [data-tests-id="artifactLabel"]',
    typeToggle: 'sdc-modal [data-tests-id="artifacttype-icon"]',
    type: (type: string) => `sdc-modal [data-tests-id="${type}"]`,
    description: 'sdc-modal [data-tests-id="description"]',
    done: 'sdc-modal [data-tests-id="upgradeVspModal-button-done"]',
    row: (label: string) => `[data-tests-id="artifactDisplayName_${label}"]`,
    magnifier: (label: string) => `[data-tests-id="gab-${label}"]`,
};

interface GabCase {
    title: string;
    create: (api: SdcApi) => Promise<CreatedAsset>;
    file: string;
    label: string;
    type: string;
    headers: number;
    rows: number;
}

async function addDeploymentArtifact(page: Page, c: GabCase): Promise<void> {
    await page.locator(ART.add).click();
    await page.locator(ART.file).setInputFiles(fixtureFile(c.file));
    await page.locator(ART.label).fill(c.label);
    await page.locator(ART.typeToggle).click();
    await page.locator(ART.type(c.type)).click();
    await page.locator(ART.description).fill('desc');
    const saved = page.waitForResponse(r => r.request().method() === 'POST' && /\/artifacts$/.test(r.url()));
    await page.locator(ART.done).click();
    const resp = await saved;
    expect(resp.status(), `adding ${c.label} failed: ${await resp.text()}`).toBe(200);
    await expect(page.locator('sdc-modal .sdc-modal')).toHaveCount(0);
    await loaderGone(page);
}

const CASES: GabCase[] = [
    {
        title: 'PM_DICTIONARY on a PNF', create: api => createPnf(api, 'PwGabPnf'),
        file: 'PNFs/pmDictionary.yml', label: 'artifactpm', type: 'PM_DICTIONARY', headers: 10, rows: 3,
    },
    {
        title: 'VES_EVENTS on a VF', create: api => api.createVf('PwGabVf'),
        file: 'VNFs/vesEvent.yml', label: 'artifactfault', type: 'VES_EVENTS', headers: 4, rows: 3,
    },
];

test.describe('generic artifact browser', () => {
    for (const c of CASES) {
        test(`${c.title}: the magnifier shows the artifact as a table`, async ({ sdcPage, api }) => {
            const asset = await c.create(api);
            await gotoWorkspaceTab(sdcPage, { id: asset.id, type: 'resource', tab: 'deployment_artifacts', cold: true });
            await loaderGone(sdcPage);

            await addDeploymentArtifact(sdcPage, c);
            const stored = Object.values((await componentData(api, asset, 'deploymentArtifacts')).deploymentArtifacts || {})
                .find((a: any) => a.artifactLabel === c.label) as any;
            expect(stored, `${asset.name} has no deployment artifact ${c.label}`).toBeTruthy();
            expect(stored.artifactType).toBe(c.type);
            await expect(sdcPage.locator(ART.row(c.label))).toBeVisible();

            await sdcPage.locator(ART.magnifier(c.label)).click();
            const gab = sdcPage.locator('sdc-modal');
            await expect(gab.locator('datatable-body-row')).toHaveCount(c.rows);
            await expect(gab.locator('datatable-header-cell')).toHaveCount(c.headers);
        });
    }
});
