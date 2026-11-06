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
 * Attributes & Outputs: declaring instance attributes as VF outputs and deleting one, checked on
 * the backend and in the CSAR's main template (outputs and substitution_mappings.attributes).
 */

import { test, expect, gotoWorkspaceTab, loaderGone } from './fixtures/sdc';
import {
    seedNetworkVfcs, addInstance, getComponent, downloadToscaCsar, resourceTemplate,
} from './fixtures/design';

test.describe('Attributes & Outputs design', () => {
    test.describe.configure({ timeout: 180_000 });
    test.use({ actionTimeout: 20_000 });

    test('instance attributes declared as outputs, minus a deleted one, reach the CSAR', async ({ sdcPage: page, api }) => {
        const { nf } = await seedNetworkVfcs(api);
        const vf = await api.createVf('PwDesignOutputs');
        const instance = await addInstance(api, vf, nf);
        const outputName = (attr: string) => `${instance.name.replace(/[\s-]/g, '').toLowerCase()}_${attr}`;

        // The page selects SELF on load and renders whichever attributes response arrives last, so
        // selecting the instance before SELF's response is in shows SELF's table instead.
        const selfLoaded = page.waitForResponse((r) => r.request().method() === 'GET'
            && r.url().includes(`/services/${vf.id}/attributes`), { timeout: 30_000 });
        await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'attributes_outputs' });
        await loaderGone(page);
        await selfLoaded;
        await page.locator('hierarchy-navigation').locator(`[data-tests-id="${instance.name}"]`).click();
        const attributesTab = page.locator('[data-tests-id="Attributes"][role="tab"]');
        const outputsTab = page.locator('[data-tests-id="Outputs"][role="tab"]');
        const outputRow = (attr: string) => page.locator('.attribute-name', { hasText: outputName(attr) });

        for (const attr of ['attr_1', 'attr_2', 'attr_3']) {
            await attributesTab.click();
            await page.locator(`checkbox[data-tests-id="${attr}"]`).click();
            await page.locator('[data-tests-id="declare-button declare-output"]').click();
            const dialog = page.locator('.custom-modal').filter({ has: page.locator('#myText') }).filter({ visible: true });
            const created = page.waitForResponse((r) => r.request().method() === 'POST'
                && /\/create\/outputs$/.test(r.url()), { timeout: 30_000 });
            await dialog.locator('[data-tests-id="Save"]').click();
            expect((await created).ok(), `declaring ${attr} as an output was rejected`).toBe(true);
            await outputsTab.click();
            await expect(outputRow(attr)).toHaveCount(1);
        }

        const deleted = page.waitForResponse((r) => r.request().method() === 'DELETE'
            && /\/delete\/[^/]+\/output$/.test(r.url()), { timeout: 30_000 });
        await page.locator(`[data-tests-id="delete-${outputName('attr_2')}"]`).click();
        await page.locator('[data-tests-id="Delete"]').click();
        expect((await deleted).ok(), 'deleting the attr_2 output was rejected').toBe(true);
        await expect(outputRow('attr_2')).toHaveCount(0);
        await attributesTab.click();
        await expect(page.locator('checkbox[data-tests-id="attr_2"]'), 'attr_2 went with its output').toBeVisible();

        const stored = (await getComponent(api, vf, 'outputs')).outputs.map((o: any) => o.name).sort();
        expect(stored).toEqual([outputName('attr_1'), outputName('attr_3')]);

        const metadataOnServer = (await getComponent(api, vf, 'metadata')).metadata;
        const csar = await downloadToscaCsar(page, vf);
        const template = await csar.mainTemplate();
        expect(await csar.mainTemplatePath()).toMatch(/-template\.yml$/i);
        expect(template).toEqual(await resourceTemplate(csar, vf));
        const { metadata, topology_template: topology } = template;
        expect(metadata.name).toBe(vf.name);
        expect(metadata.description).toBe(metadataOnServer.description);
        expect(metadata.category).toBe('Generic');
        expect(metadata.subcategory).toBe('Abstract');
        expect(metadata.type).toBeTruthy();
        expect(metadata.resourceVendor).toBe(metadataOnServer.vendorName);
        expect(metadata.resourceVendorRelease).toBe(metadataOnServer.vendorRelease);
        expect(Object.keys(topology.inputs || {})).not.toHaveLength(0);
        expect(Object.keys(topology.node_templates || {})).toEqual([instance.name]);
        expect(Object.keys(topology.outputs || {}).sort()).toEqual([outputName('attr_1'), outputName('attr_3')]);
        expect(Object.keys(topology.substitution_mappings?.attributes || {}).sort())
            .toEqual([outputName('attr_1'), outputName('attr_3')]);
    });
});
