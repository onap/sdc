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

import { test, expect, gotoWorkspaceTab, loaderGone } from './fixtures/sdc';
import * as models from './fixtures/models';

const ADDITIONAL_SERVICE_DATA = 'tosca.datatypes.nfv.AdditionalServiceData';
const PROPERTY_NAME = 'AdditionalServiceData';
const LINKABLE = 'tosca.capabilities.network.Linkable';

test.describe('Models: TOSCA types imported into a model', () => {

    test('a property of an imported data type and the imported types reach the VF CSAR', async ({ sdcPage: page, api }) => {
        test.setTimeout(180_000);
        const model = await models.createModel(api, models.ETSI_MODEL);
        await models.importTypesIntoModel(api, model);
        const network = await models.importNodeTypeIntoModel(api, 'Network', model);
        const vf = await models.createVfInModel(api, model, 'PwTypeImportVf');
        const instance = await models.addInstance(api, vf, { id: network.id, originType: 'VL' });

        await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'properties_assignment' });
        await loaderGone(page);
        await page.locator('.right-column [data-tests-id="properties-add-button"]').click();
        const modal = page.locator('.custom-modal');
        await expect(modal).toBeVisible();
        await modal.locator('[data-tests-id="property-name"]').fill(PROPERTY_NAME);
        const typeSelect = modal.locator('[data-tests-id="value-property-type"]');
        await expect(typeSelect.getByRole('option', { name: ADDITIONAL_SERVICE_DATA, exact: true }),
            `the type list should offer ${ADDITIONAL_SERVICE_DATA}, which ${model} imported`).toHaveCount(1);
        await typeSelect.selectOption({ label: ADDITIONAL_SERVICE_DATA });
        await modal.locator('[data-tests-id="property-description"]').fill('Integration Test for adding property to a component');
        const created = page.waitForResponse(r => r.request().method() === 'POST' && /\/properties$/.test(r.url()));
        await modal.locator('[data-tests-id="Save"]').click();
        expect((await created).status()).toBe(200);
        await expect(page.locator(`checkbox[data-tests-id="${PROPERTY_NAME}"]`)).toBeVisible();

        const stored = (await models.getComponent(api, vf, ['properties'])).properties
            .find((p: any) => p.name === PROPERTY_NAME);
        expect(stored, `${vf.name} should own the property ${PROPERTY_NAME}`).toBeTruthy();
        expect(stored.type).toBe(ADDITIONAL_SERVICE_DATA);

        const csar = await models.downloadToscaModelCsar(page, vf);
        const vfTemplate = models.templatePathFor('resource', vf.name);
        const vfInterface = vfTemplate.replace('-template.yml', '-template-interface.yml');
        // The parent's additional_type_definitions.yaml keeps its name; the derived model's own copy,
        // which holds the uploaded types, is prefixed with the model name.
        const modelTypes = `Definitions/${model}-additional_type_definitions.yaml`;
        expect(csar.entries().sort()).toEqual([
            `Definitions/${model}.yaml`,
            modelTypes,
            'Definitions/additional_type_definitions.yaml',
            'Definitions/etsi_nfv_sol001_nsd_types.yaml',
            'Definitions/etsi_nfv_sol001_vnfd_types.yaml',
            `Definitions/resource-${network.name}-template.yml`,
            vfInterface,
            vfTemplate,
            'TOSCA-Metadata/TOSCA.meta',
            'csar.meta',
        ].sort());

        const imported = await csar.yaml(modelTypes);
        expect(Object.keys(imported.data_types || {})).toContain(ADDITIONAL_SERVICE_DATA);
        expect(Object.keys(imported.capability_types || {})).toContain(LINKABLE);
        expect(await csar.text(`Definitions/resource-${network.name}-template.yml`)).toContain(LINKABLE);

        const main = await csar.mainTemplate();
        const node = main.topology_template.node_templates[instance.name];
        expect(node, `${vfTemplate} should hold the ${instance.name} node template`).toBeTruthy();
        expect(node.type).toBe('tosca.nodes.network.Network');
        expect(node.metadata.model).toBe(model);

        const vfType = (await csar.yaml(vfInterface)).node_types[main.topology_template.substitution_mappings.node_type];
        expect(vfType.properties[PROPERTY_NAME].type).toBe(ADDITIONAL_SERVICE_DATA);
    });
});
