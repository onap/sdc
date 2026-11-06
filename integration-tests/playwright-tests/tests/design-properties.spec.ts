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
 * Properties Assignment: adding component properties and inputs, giving properties values,
 * declaring inputs from component and instance properties, and input metadata. Each is checked on
 * the backend and in the generated template.
 */

import { Page } from '@playwright/test';
import { test, expect, gotoWorkspaceTab, loaderGone } from './fixtures/sdc';
import {
    seedNetworkVfcs, addInstance, addProperties, addInput, getComponent,
    downloadToscaCsar, downloadToscaTemplate, resourceTemplate,
} from './fixtures/design';

// The page saves through the services collection whatever the component type.
const SAVE_PROPERTIES = /\/v1\/catalog\/services\/[^/]+\/properties$/;

async function gotoPropertiesAssignment(page: Page, vf: { id: string }, tab: 'Properties' | 'Inputs' = 'Properties'): Promise<void> {
    await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'properties_assignment' });
    await loaderGone(page);
    if (tab === 'Inputs') {
        await page.locator('[data-tests-id="Inputs"][role="tab"]').click();
    }
    await expect(page.locator('.properties-table .table-row').first()).toBeVisible({ timeout: 30_000 });
}

/** The Add Property / Add Input form; lists and maps get string entries, as AddPropertyModal chose. */
async function createInModal(page: Page, name: string, type: string): Promise<void> {
    // A closed custom modal can linger in the DOM, hidden, until the next one opens.
    const modal = page.locator('.custom-modal').filter({ has: page.locator('property-creator') }).filter({ visible: true });
    await modal.locator('[data-tests-id="property-name"]').fill(name);
    await modal.locator('select[data-tests-id="value-property-type"]').selectOption({ label: type });
    if (type === 'list' || type === 'map') {
        await modal.locator('select[data-tests-id="value-property-schema-type"]').selectOption({ label: 'string' });
    }
    await modal.locator('[data-tests-id="property-description"]').fill('Integration Test for adding property to a component');
    await modal.locator('[data-tests-id="Save"]').click();
    await expect(modal).toHaveCount(0);
}

test.describe('Properties Assignment design', () => {
    test.describe.configure({ timeout: 180_000 });
    test.use({ actionTimeout: 20_000 });

    test('component properties of every type are added, given values, and reach the CSAR', async ({ sdcPage: page, api }) => {
        const vf = await api.createVf('PwDesignProps');
        const types: Record<string, string> = {
            property1: 'string', property2: 'integer', property3: 'boolean',
            property4: 'list', property5: 'map', property6: 'scalar-unit.size',
        };
        await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'properties_assignment' });
        await loaderGone(page);
        for (const [name, type] of Object.entries(types)) {
            await page.locator('[data-tests-id="properties-add-button"]').click();
            await createInModal(page, name, type);
            await expect(page.locator(`checkbox[data-tests-id="${name}"]`)).toBeVisible();
        }

        const row = (name: string) => page.locator('.table-row').filter({ has: page.locator(`checkbox[data-tests-id="${name}"]`) });
        await row('property1').locator('[data-tests-id="value-prop-property1"]').fill('Integration Test');
        await row('property2').locator('[data-tests-id="value-prop-property2"]').fill('100');
        await row('property3').locator('[data-tests-id="value-prop-property3"]').selectOption({ label: 'TRUE' });
        for (const [i, v] of ['PropListV1', 'PropListV2', 'PropListV3'].entries()) {
            await row('property4').locator('[data-tests-id="add-to-list-property4"]').click();
            await row('property4').locator(`[data-tests-id="value-prop-property4.${i}"]`).fill(v);
        }
        for (const [i, [k, v]] of [['PropMapKey1', 'PropMapValue1'], ['PropMapKey2', 'PropMapValue2'], ['PropMapKey3', 'PropMapValue3']].entries()) {
            await row('property5').locator('[data-tests-id="add-to-list-property5"]').click();
            await row('property5').locator(`[data-tests-id="value-prop-key-property5.${i}"]`).fill(k);
            await row('property5').locator(`[data-tests-id="value-prop-property5.${i}"]`).fill(v);
        }
        await row('property6').locator('[data-tests-id="value-prop-property6"]').fill('500GB');
        const saved = page.waitForResponse((r) => r.request().method() === 'PUT' && SAVE_PROPERTIES.test(r.url()), { timeout: 30_000 });
        await page.locator('[data-tests-id="properties-save-button"]').click();
        expect((await saved).ok(), 'saving the property values was rejected').toBe(true);

        const stored = Object.fromEntries((await getComponent(api, vf, 'properties')).properties
            .map((p: any) => [p.name, { type: p.type, value: p.value }]));
        expect(stored).toEqual({
            property1: { type: 'string', value: 'Integration Test' },
            property2: { type: 'integer', value: '100' },
            property3: { type: 'boolean', value: 'true' },
            property4: { type: 'list', value: expect.any(String) },
            property5: { type: 'map', value: expect.any(String) },
            property6: { type: 'scalar-unit.size', value: '500GB' },
        });
        expect(JSON.parse(stored.property4.value)).toEqual(['PropListV1', 'PropListV2', 'PropListV3']);
        expect(JSON.parse(stored.property5.value)).toEqual(
            { PropMapKey1: 'PropMapValue1', PropMapKey2: 'PropMapValue2', PropMapKey3: 'PropMapValue3' });

        const csar = await downloadToscaCsar(page, vf);
        const nodeTypes = (await resourceTemplate(csar, vf, 'interface')).node_types;
        const nodeType: any = Object.values(nodeTypes)[0];
        expect(Object.keys(nodeType.properties || {})).toEqual(expect.arrayContaining(Object.keys(types)));
    });

    test('after saving several property values the table shows what was saved', async ({ sdcPage: page, api }) => {
        const vf = await api.createVf('PwDesignPropsEcho');
        await addProperties(api, vf, { property1: 'string', property2: 'integer' });
        await gotoPropertiesAssignment(page, vf);
        await page.locator('[data-tests-id="value-prop-property1"]').fill('Integration Test');
        await page.locator('[data-tests-id="value-prop-property2"]').fill('100');
        const saved = page.waitForResponse((r) => r.request().method() === 'PUT' && SAVE_PROPERTIES.test(r.url()), { timeout: 30_000 });
        await page.locator('[data-tests-id="properties-save-button"]').click();
        expect((await saved).ok()).toBe(true);
        expect(Object.fromEntries((await getComponent(api, vf, 'properties')).properties.map((p: any) => [p.name, p.value])))
            .toEqual({ property1: 'Integration Test', property2: '100' });

        await expect(page.locator('[data-tests-id="properties-save-button"]'), 'the page still has unsaved changes').toBeDisabled();
        await expect(page.locator('[data-tests-id="value-prop-property1"]')).toHaveValue('Integration Test');
        await expect(page.locator('[data-tests-id="value-prop-property2"]')).toHaveValue('100');
    });

    test('component inputs are added and reach the TOSCA template', async ({ sdcPage: page, api }) => {
        const vf = await api.createVf('PwDesignInputs');
        const inputs: Record<string, string> = { input1: 'string', input2: 'integer', input3: 'boolean' };
        await gotoPropertiesAssignment(page, vf, 'Inputs');
        for (const [name, type] of Object.entries(inputs)) {
            await page.locator('[data-tests-id="inputs-add-button"]').click();
            await createInModal(page, name, type);
            await expect(page.locator('.properties-table .property-name', { hasText: new RegExp(`^${name}$`) })).toHaveCount(1);
        }

        const stored = (await getComponent(api, vf, 'inputs')).inputs;
        expect(Object.fromEntries(stored.filter((i: any) => i.name in inputs).map((i: any) => [i.name, i.type]))).toEqual(inputs);

        const template = await downloadToscaTemplate(page, vf);
        expect(Object.keys(template.topology_template.inputs)).toEqual(expect.arrayContaining(Object.keys(inputs)));
    });

    test('inputs declared from a component and an instance property reach the TOSCA template', async ({ sdcPage: page, api }) => {
        const { nf } = await seedNetworkVfcs(api);
        const vf = await api.createVf('PwDesignDeclare');
        await addProperties(api, vf, { property1: 'string' });
        const instance = await addInstance(api, vf, nf);

        const declare = async (property: string, inputName: string) => {
            await page.locator(`checkbox[data-tests-id="${property}"]`).click();
            await page.locator('[data-tests-id="declare-button declare-input"]').click();
            const modal = page.locator('.custom-modal').filter({ has: page.locator('declare-input') }).filter({ visible: true });
            // The dialog reads the name from its keyup handler, which fill() does not fire.
            await modal.locator('#myText').clear();
            await modal.locator('#myText').pressSequentially(inputName);
            const created = page.waitForResponse((r) => r.request().method() === 'POST'
                && /\/create\/inputs$/.test(r.url()), { timeout: 30_000 });
            await modal.locator('[data-tests-id="Save"]').click();
            expect((await created).ok(), `declaring ${inputName} was rejected`).toBe(true);
            await expect(modal).toHaveCount(0);
        };

        await gotoPropertiesAssignment(page, vf);
        await declare('property1', 'inputName1');
        await page.locator('hierarchy-navigation').locator(`[data-tests-id="${instance.name}"]`).click();
        await expect(page.locator('checkbox[data-tests-id="resourceSubtype"]')).toBeVisible();
        await declare('resourceSubtype', 'inputName2');
        await page.locator('[data-tests-id="Inputs"][role="tab"]').click();
        for (const name of ['inputName1', 'inputName2']) {
            await expect(page.locator('.properties-table .property-name', { hasText: new RegExp(`^${name}$`) })).toHaveCount(1);
        }

        const template = await downloadToscaTemplate(page, vf);
        const topology = template.topology_template;
        expect(Object.keys(topology.inputs).filter(n => n.includes('inputName')).sort()).toEqual(['inputName1', 'inputName2']);
        expect(Object.keys(topology.substitution_mappings?.properties || {}).filter(n => n.includes('property1')))
            .toEqual(['property1']);
        expect(topology.node_templates[instance.name].properties?.resourceSubtype).toEqual({ get_input: 'inputName2' });
    });

    test('metadata added to an input reaches the TOSCA template', async ({ sdcPage: page, api }) => {
        const vf = await api.createVf('PwDesignInputMeta');
        await addInput(api, vf, 'metaInput');
        const metadata = { key: 'Key', value: 'Test' };

        await gotoPropertiesAssignment(page, vf, 'Inputs');
        const row = page.locator('.properties-table .table-row')
            .filter({ has: page.locator('.property-name', { hasText: /^metaInput$/ }) });
        await row.getByRole('button', { name: 'Add metadata' }).click();
        const pair = row.locator('.metadata-container input');
        await expect(pair).toHaveCount(2);
        await pair.nth(0).fill(metadata.key);
        await pair.nth(1).fill(metadata.value);
        const saved = page.waitForResponse((r) => r.request().method() === 'POST'
            && /\/update\/inputs$/.test(r.url()), { timeout: 30_000 });
        await page.locator('[data-tests-id="properties-save-button"]').click();
        expect((await saved).ok(), 'saving the input metadata was rejected').toBe(true);

        const stored = (await getComponent(api, vf, 'inputs')).inputs.find((i: any) => i.name === 'metaInput');
        expect(stored.metadata).toEqual({ [metadata.key]: metadata.value });

        const template = await downloadToscaTemplate(page, vf);
        expect(template.topology_template.inputs.metaInput?.metadata).toEqual({ [metadata.key]: metadata.value });
    });
});
