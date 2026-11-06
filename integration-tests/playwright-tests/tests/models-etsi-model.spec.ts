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

import { test, expect, SEL, SdcApi, CreatedAsset, gotoWorkspaceTab, loaderGone, uniqueName } from './fixtures/sdc';
import { Csar, fixtureFile } from './fixtures/csar';
import * as models from './fixtures/models';

const ETSI_PACKAGE = 'ETSI/Vnf-ETSI-SOL001-2.5.1.csar';
const ETSI_NFV_NETWORK_SERVICE = 'ETSI NFV Network Service';

const VF_PROPERTIES: Record<string, string> = {
    descriptor_id: 'descriptor_id',
    descriptor_version: 'v1.0.1',
    flavour_description: 'flavour_description',
    flavour_id: 'flavour_id',
    product_name: 'product_name',
    provider: 'provider',
    software_version: 'v1.0.1',
};

async function onboardEtsiVsp(api: SdcApi) {
    const vlm = await api.createVlm('PwEtsiVlm');
    await api.submitVlm(vlm);
    return models.onboardVsp(api, vlm, fixtureFile(ETSI_PACKAGE), { model: models.ETSI_MODEL, namePrefix: 'PwEtsiVsp' });
}

/** The checks EtsiModelUiTests makes on the VF's own template, in a VF CSAR and in a service CSAR alike. */
async function checkEtsiVfTemplate(csar: Csar, vfName: string): Promise<void> {
    const path = models.templatePathFor('resource', vfName);
    const topology = (await csar.yaml(path)).topology_template;
    expect(topology, `${path} should contain a topology_template`).toBeTruthy();
    expect(topology.substitution_mappings.node_type).toBe('org.openecomp.resource.EtsiDummyVnf');
    const nodes = Object.keys(topology.node_templates);
    expect(nodes).toEqual(expect.arrayContaining(['external_connection_point', 'vnf_virtual_link']));
    expect(nodes, 'the substitutable node must not be a node template').not.toContain('etsi_dummy_vnf');
}

test.describe(`Models: ${models.ETSI_MODEL} VSP to VF and service`, () => {

    test('imports an ETSI VSP into a VF of its model, whose properties and CSAR follow the package', async ({ sdcPage: page, api }) => {
        test.setTimeout(240_000);
        const vsp = await onboardEtsiVsp(api);

        await page.locator('[data-tests-id="importButtonsArea"]').hover();
        await page.locator('[data-tests-id="importButtonsVSP"]').click();
        const table = page.locator('[data-tests-id="importVspTable"]');
        await expect(table).toBeVisible({ timeout: 30_000 });
        await table.locator('[data-tests-id="onboarding-search-input"]').fill(vsp.name);
        const row = table.locator('datatable-body-row').filter({ hasText: vsp.name });
        await expect(row).toHaveCount(1, { timeout: 30_000 });
        await row.click();
        await table.locator('[data-tests-id="import-csar"]').click();

        await expect(page.locator(SEL.name)).toHaveValue(vsp.name, { timeout: 60_000 });
        await expect(page.locator('[data-tests-id="selectModelName"]')).toHaveValue(new RegExp(`${models.ETSI_MODEL}$`));
        const created = page.waitForResponse(r => r.request().method() === 'POST' && /\/v1\/catalog\/resources$/.test(r.url()),
            { timeout: 120_000 });
        await page.locator(SEL.chromeCreateButton).click();
        const resp = await created;
        const body = await resp.json();
        expect(resp.status(), JSON.stringify(body).slice(0, 400)).toBe(201);
        expect(body.model).toBe(models.ETSI_MODEL);
        expect(body.csarUUID).toBe(vsp.id);
        const vf = await models.certify(api, { id: body.uniqueId, name: vsp.name, type: 'resource' });

        await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'properties_assignment' });
        await loaderGone(page);
        for (const [name, value] of Object.entries(VF_PROPERTIES)) {
            await expect(page.locator(`input[data-tests-id="value-prop-${name}"]`), name).toHaveValue(value, { timeout: 30_000 });
        }
        const vnfmInfo = page.locator('input[data-tests-id^="value-prop-vnfm_info"]');
        await expect(vnfmInfo).toHaveCount(1);
        await expect(vnfmInfo).toHaveValue('etsivnfm:v1.0.1');

        const stored = (await models.getComponent(api, vf, ['properties'])).properties;
        const byName = Object.fromEntries(stored.map((p: any) => [p.name, p.value ?? p.defaultValue]));
        for (const [name, value] of Object.entries(VF_PROPERTIES)) {
            expect(byName[name], name).toBe(value);
        }
        expect(JSON.parse(byName.vnfm_info)).toEqual(['etsivnfm:v1.0.1']);

        await checkEtsiVfTemplate(await models.downloadToscaModelCsar(page, vf), vf.name);
    });

    test('creates a service in the ETSI model whose CSAR carries the VF template', async ({ sdcPage: page, api }) => {
        test.setTimeout(240_000);
        const vsp = await onboardEtsiVsp(api);
        const vf = await models.certify(api, await models.createVfFromVsp(api, vsp, models.ETSI_MODEL));

        await page.locator('.w-sdc-dashboard-card-new').first().hover();
        await page.locator('[data-tests-id="createServiceButton"]').click();
        await expect(page.locator(SEL.name)).toBeVisible({ timeout: 30_000 });
        const name = uniqueName('PwSvcForModel');
        await page.locator(SEL.name).fill(name);
        await page.locator('[data-tests-id="selectModelName"]').selectOption({ label: models.ETSI_MODEL });
        await page.locator(SEL.category).selectOption({ label: ETSI_NFV_NETWORK_SERVICE });
        await page.locator(SEL.description).fill('aDescription');
        const created = page.waitForResponse(r => r.request().method() === 'POST' && /\/v1\/catalog\/services$/.test(r.url()),
            { timeout: 60_000 });
        await page.locator(SEL.chromeCreateButton).click();
        const resp = await created;
        const body = await resp.json();
        expect(resp.status(), JSON.stringify(body).slice(0, 400)).toBe(201);
        expect(body.model).toBe(models.ETSI_MODEL);
        const service: CreatedAsset = { id: body.uniqueId, name, type: 'service' };

        const instance = await models.addInstance(api, service, { id: vf.id, originType: 'VF' });
        const certified = await models.certify(api, service);
        const csar = await models.downloadToscaModelCsar(page, certified);
        const main = await csar.mainTemplate();
        expect(main.metadata.model).toBe(models.ETSI_MODEL);
        expect(Object.keys(main.topology_template.node_templates)).toContain(instance.name);
        await checkEtsiVfTemplate(csar, vf.name);
    });
});
