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

import { Page } from '@playwright/test';
import { test, expect, SEL, CreatedAsset, gotoWorkspaceTab, loaderGone, uniqueName } from './fixtures/sdc';
import { selectNode } from './fixtures/design';
import { Csar, fixtureFile } from './fixtures/csar';
import * as models from './fixtures/models';

const VIRTUAL_LINKABLE = 'tosca.capabilities.nfv.VirtualLinkable';
const EDITED: Record<string, string> = {
    designer: 'designer1',
    descriptor_id: 'descriptor_id1',
    flavour_id: 'flavour_id1',
    invariant_id: 'invariant_id1',
    name: 'name1',
    version: 'version1',
    service_availability_level: '1',
};


test.describe('Models: ETSI NFV network service', () => {

    test('a VF with an externalised virtual link in an ETSI NS yields the expected NSD package', async ({ sdcPage: page, api }) => {
        test.setTimeout(300_000);
        const vlm = await api.createVlm('PwNsVlm');
        await api.submitVlm(vlm);
        const vsp = await models.onboardVsp(api, vlm, fixtureFile('VNFs/etsi-vnf-virtual-linkable.csar'), { namePrefix: 'PwNsVsp' });
        const vfDraft = await models.createVfFromVsp(api, vsp);

        await gotoWorkspaceTab(page, { id: vfDraft.id, type: 'resource', tab: 'composition' });
        await loaderGone(page);
        await selectNode(page, 'mgmt_port');
        await page.locator('[data-tests-id="detail-tab-requirements-capabilities"]').click();
        await page.locator('[data-tests-id="Requirements-accordion"]').click();
        const external = page.locator('checkbox[data-tests-id="checkbox-external-req-external_virtual_link"]');
        const marked = page.waitForResponse(r => r.request().method() === 'PUT'
            && /\/requirementName\/external_virtual_link$/.test(r.url()));
        await external.locator('label').click();
        expect((await marked).status()).toBe(200);
        await expect(external.locator('input')).toBeChecked();

        const draft = await models.getComponent(api, vfDraft, ['componentInstances']);
        const mgmtPort = draft.componentInstances.find((i: any) => i.name === 'mgmt_port');
        const requirement = ([] as any[]).concat(...Object.values(mgmtPort.requirements))
            .find((r: any) => r.name === 'external_virtual_link');
        expect(requirement.external, 'the backend should store mgmt_port.external_virtual_link as external').toBe(true);
        const vf = await models.certify(api, vfDraft);

        await page.locator(SEL.homeBreadcrumb).click();
        await page.locator('.w-sdc-dashboard-card-new').first().hover();
        await page.locator('[data-tests-id="createServiceButton"]').click();
        await expect(page.locator(SEL.name)).toBeVisible({ timeout: 30_000 });
        const serviceName = uniqueName('PwEtsiNfvNs');
        await page.locator(SEL.name).fill(serviceName);
        await page.locator(SEL.category).selectOption({ label: 'ETSI NFV Network Service' });
        await page.locator('select[data-tests-id="ETSI Version"]').selectOption('2.5.1');
        await page.locator(SEL.description).fill('aDescription');
        const created = page.waitForResponse(r => r.request().method() === 'POST' && /\/v1\/catalog\/services$/.test(r.url()),
            { timeout: 60_000 });
        await page.locator(SEL.chromeCreateButton).click();
        const resp = await created;
        const body = await resp.json();
        expect(resp.status(), JSON.stringify(body).slice(0, 400)).toBe(201);
        const service: CreatedAsset = { id: body.uniqueId, name: serviceName, type: 'service' };

        const vnf1 = await models.addInstance(api, service, { id: vf.id, originType: 'VF' }, { x: 200, y: 200 });
        const vnf2 = await models.addInstance(api, service, { id: vf.id, originType: 'VF' }, { x: 400, y: 200 });
        const link = await models.addInstance(api, service,
            { id: await models.normativeId(api, 'NsVirtualLink'), originType: 'VL' }, { x: 300, y: 400 });
        for (const vnf of [vnf1, vnf2]) {
            // A VF exposes an externalised instance requirement as <instance>.<requirement>.
            await models.associate(api, service, vnf, 'mgmt_port.external_virtual_link', link, VIRTUAL_LINKABLE);
        }

        await gotoWorkspaceTab(page, { id: service.id, type: 'service', tab: 'properties_assignment' });
        await loaderGone(page);
        for (const name of [...Object.keys(EDITED), 'ns_profile']) {
            await expect(page.locator(`checkbox[data-tests-id="${name}"]`), `service property ${name}`).toBeVisible({ timeout: 30_000 });
        }
        for (const [name, value] of Object.entries(EDITED)) {
            await page.locator(`input[data-tests-id="value-prop-${name}"]`).fill(value);
        }
        const saved = page.waitForResponse(r => r.request().method() === 'PUT' && /\/properties$/.test(r.url()));
        await page.locator('[data-tests-id="properties-save-button"]').click();
        expect((await saved).ok()).toBe(true);
        await loaderGone(page);
        for (const [name, value] of Object.entries(EDITED)) {
            await expect(page.locator(`input[data-tests-id="value-prop-${name}"]`), `${name} after Save`).toHaveValue(value);
        }
        await expect(page.locator('[data-tests-id="properties-save-button"]')).toBeDisabled();

        const stored = Object.fromEntries((await models.getComponent(api, service, ['properties'])).properties
            .map((p: any) => [p.name, p.value]));
        for (const [name, value] of Object.entries(EDITED)) {
            expect(stored[name], name).toBe(value);
        }

        await gotoWorkspaceTab(page, { id: service.id, type: 'service', tab: 'general' });
        const csar = await models.downloadToscaModelCsar(page, service);
        const packages = csar.entries().filter(e => e.startsWith('Artifacts/ETSI_PACKAGE'));
        expect(packages, `entries: ${csar.entries().join(', ')}`).toHaveLength(1);
        const packageEntry = packages[0];
        const baseName = packageEntry.split('/').pop()!.replace(/\.(zip|csar)$/, '');
        let nsdCsar: Csar;
        if (packageEntry.endsWith('.zip')) {
            const signed = await Csar.fromBuffer(await csar.bytes(packageEntry), packageEntry);
            expect(signed.entries().sort()).toEqual([`${baseName}.cert`, `${baseName}.cms`, `${baseName}.csar`]);
            nsdCsar = await Csar.fromBuffer(await signed.bytes(`${baseName}.csar`), `${baseName}.csar`);
        } else {
            expect(packageEntry.endsWith('.csar'), `unexpected ETSI package entry ${packageEntry}`).toBe(true);
            nsdCsar = await Csar.fromBuffer(await csar.bytes(packageEntry), packageEntry);
        }

        const nsd = (await nsdCsar.yaml(`Definitions/${baseName}.yaml`)).topology_template;
        const serviceNodeType = `org.openecomp.service.${serviceName.charAt(0).toUpperCase()}${serviceName.slice(1).toLowerCase()}`;
        expect(nsd.substitution_mappings.node_type).toBe(serviceNodeType);
        for (const vnf of [vnf1, vnf2]) {
            const node = nsd.node_templates[vnf.name];
            expect(node, `the NSD should hold ${vnf.name}`).toBeTruthy();
            expect(node.type).toBe('org.openecomp.resource.EtsiVnfVirtualLinkable');
            expect(node.requirements).toEqual([{ external_virtual_link: { capability: 'virtual_linkable', node: link.name } }]);
        }
        expect(nsd.node_templates[link.name].type).toBe('tosca.nodes.nfv.NsVirtualLink');
        const properties = nsd.node_templates[serviceNodeType].properties;
        expect(Object.keys(properties).sort()).toEqual(Object.keys(EDITED).sort());
        for (const [name, value] of Object.entries(EDITED)) {
            expect(properties[name], name).toEqual(name === 'service_availability_level' ? Number(value) : value);
        }
    });
});
