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
 * Service-template design on the Composition canvas: relationships, instance interface operations,
 * directive node filters, substitution filters and instance names, each checked against the
 * backend and, where the scenario is about the generated template, against the downloaded CSAR.
 */

import { Locator } from '@playwright/test';
import { test, expect } from './fixtures/sdc';
import {
    seedNetworkVfcs, addInstance, addProperties, getComponent, gotoComposition, dragLink, selectNode,
    downloadToscaCsar, downloadToscaTemplate, resourceTemplate,
} from './fixtures/design';

/** The properties ServiceTemplateDesignUiTests adds to its VF, one per type the editors handle. */
const DESIGN_PROPERTIES = {
    property1: 'string', property2: 'integer', property3: 'boolean',
    property4: 'list', property5: 'map', property6: 'scalar-unit.size',
};

test.describe('Composition design', () => {
    test.describe.configure({ timeout: 180_000 });
    test.use({ actionTimeout: 20_000 });

    test('a DependsOn relationship with an interface operation reaches the CSAR', async ({ sdcPage: page, api }) => {
        const { nf, ns } = await seedNetworkVfcs(api);
        const vf = await api.createVf('PwDesignRel');
        const from = await addInstance(api, vf, nf, { x: 200, y: 200 });
        const to = await addInstance(api, vf, ns, { x: 500, y: 200 });

        await gotoComposition(page, vf, [from.name, to.name]);
        await dragLink(page, from.name, to.name);

        const wizard = page.locator('multi-steps-wizard');
        const next = wizard.locator('.ng2-modal-footer button:has(.white-arrow-next)');
        await wizard.locator('select[data-tests-id="value-select"]').selectOption('tosca.capabilities.Node');
        await next.click();
        await expect(wizard.locator('.step-title.active')).toContainText(to.name);
        await wizard.locator('select[data-tests-id="value-select"]').selectOption('tosca.capabilities.Node');
        await next.click();
        await expect(wizard.locator('.step-title.active')).toContainText('Properties');
        // The operation form copies the interface types once, when it opens, so opening it before
        // this response is in leaves its Interface dropdown empty for good (SDC-5251).
        const interfaceTypes = page.waitForResponse((r) => r.request().method() === 'GET'
            && r.url().includes('/interfaceLifecycleTypes'), { timeout: 30_000 });
        await next.click();
        await expect(wizard.locator('.step-title.active')).toContainText('Operations');
        await interfaceTypes;

        await wizard.locator('[data-tests-id="addInterfaceOperation"]').click();
        const creator = wizard.locator('app-create-interface-operation');
        await creator.locator('[data-tests-id="interface-name-icon"]').click();
        await creator.locator('.sdc-dropdown__option', { hasText: 'Standard' }).first().click();
        await creator.locator('[data-tests-id="operation-name-icon"]').click();
        await creator.locator('.sdc-dropdown__option', { hasText: /^\s*create\s*$/ }).click();
        await creator.locator('#implementationInput').fill('IntegrationTest');
        await creator.locator('[data-tests-id="addInputParameter"]').click();
        await creator.locator('#propertyAssignmentNameInput').fill('InputName1');
        await creator.locator('#propertyAssignmentValueInput').fill('InputValue1');
        await creator.locator('[data-tests-id="addBtn"]').click();
        await expect(wizard.locator('.operation-list')).toContainText('create');

        const created = page.waitForResponse((r) => r.request().method() === 'POST'
            && /\/resourceInstance\/associate$/.test(r.url()), { timeout: 30_000 });
        await wizard.locator('.ng2-modal-footer button', { hasText: 'Finish' }).click();
        expect((await created).status(), 'the relationship was rejected').toBe(200);
        await expect(wizard).toHaveCount(0);

        // The backend resolves relations only when the instances are read with them.
        const { componentInstancesRelations: relations } =
            await getComponent(api, vf, 'componentInstances', 'componentInstancesRelations');
        expect(relations, 'the backend holds no relationship').toHaveLength(1);
        const { relation, operations } = relations[0].relationships[0];
        expect(relation.relationship.type).toBe('tosca.relationships.DependsOn');
        expect(relation.capabilityOwnerId, 'the capability is not the source node\'s').toBe(from.id);
        expect(relation.requirementOwnerId, 'the requirement is not the target node\'s').toBe(to.id);
        expect(operations).toEqual([expect.objectContaining({
            interfaceType: 'tosca.interfaces.node.lifecycle.Standard', operationType: 'create',
            implementation: 'IntegrationTest',
            inputs: [expect.objectContaining({ name: 'InputName1', value: 'InputValue1' })],
        })]);

        const csar = await downloadToscaCsar(page, vf);
        const template = await resourceTemplate(csar, vf);
        expect(template.tosca_definitions_version).toBe('tosca_simple_yaml_1_3');
        expect(Object.keys(template.topology_template.node_templates).sort()).toEqual([from.name, to.name].sort());
        const relationshipTemplates = JSON.stringify(template.topology_template.relationship_templates || {});
        for (const expected of ['tosca.relationships.DependsOn', 'interfaces', 'Standard', 'create',
            'IntegrationTest', 'inputs', 'InputName1', 'InputValue1']) {
            expect(relationshipTemplates, `relationship_templates lacks '${expected}'`).toContain(expected);
        }
        const nodeTypes = JSON.stringify((await resourceTemplate(csar, vf, 'interface')).node_types);
        for (const expected of ['capabilities', 'requirements', 'relationship', 'tosca.relationships.DependsOn']) {
            expect(nodeTypes, `the interface template's node_types lack '${expected}'`).toContain(expected);
        }
    });
    test('an instance interface operation update reaches the TOSCA template', async ({ sdcPage: page, api }) => {
        const { ns } = await seedNetworkVfcs(api);
        const vf = await api.createVf('PwDesignOp');
        const node = await addInstance(api, vf, ns);
        const update = {
            description: 'IT for updating an Interface Operation',
            implementation: 'MyIntegrationTestImplementationName',
            input: { name: 'My_IT_InputName', type: 'string', value: 'My_IT_InputValue' },
        };

        await gotoComposition(page, vf, [node.name]);
        await selectNode(page, node.name);
        await page.locator('[data-tests-id="detail-tab-interface-operations"] button[role="tab"]').click();
        const operations = page.locator('.interface-operations .interface-row');
        const create = operations.locator('.data-row').filter({ has: page.locator('.operation-name', { hasText: /^create$/ }) });
        await expect(operations.locator('.interface-name')).toHaveText('Standard');
        await create.locator('.operation-name').click();

        const modal = page.locator('.custom-modal').filter({ hasText: 'Edit Operation' });
        await modal.locator('[data-tests-id="interface-operation-description"]').fill(update.description);
        await modal.locator('[data-tests-id="interface-operation-implementation-name"]').fill(update.implementation);
        await modal.locator('[data-tests-id="add-input.add-input-link"]').click();
        await modal.locator('[data-tests-id="add-input.input-name"]').fill(update.input.name);
        await modal.locator('[data-tests-id="add-input.input-type-filter-bar"]').click();
        await modal.locator('[data-tests-id="add-input.input-type-filter-bar"]').fill(update.input.type);
        await modal.locator(`li[data-tests-id="${update.input.type}"]`).click();
        await modal.locator('[data-tests-id="add-input.add-input-btn"]').click();
        const inputItem = modal.locator('.input-tree li.root').filter({ hasText: `${update.input.name}:` });
        await expect(inputItem.locator('[data-tests-id="input-type"]')).toHaveText(update.input.type);
        await inputItem.getByRole('button', { name: update.input.name, exact: true }).click();
        await inputItem.locator('li.input-value > input').fill(update.input.value);

        const saved = page.waitForResponse((r) => r.request().method() === 'PUT'
            && /\/componentInstance\/[^/]+\/interfaceOperation$/.test(r.url()), { timeout: 30_000 });
        await modal.locator('[data-tests-id="Save"]').click();
        expect((await saved).ok(), 'the operation update was rejected').toBe(true);
        await expect(modal).toHaveCount(0);

        await expect(create.locator('.field-description'), 'the table does not show the new description')
            .toHaveText(update.description);
        await create.locator('.operation-name').click();
        await expect(modal.locator('[data-tests-id="interface-operation-description"]')).toHaveValue(update.description);
        await expect(modal.locator('[data-tests-id="interface-operation-implementation-name"]')).toHaveValue(update.implementation);
        await modal.locator('[data-tests-id="Cancel"]').click();

        const template = await downloadToscaTemplate(page, vf);
        const operation = template.topology_template.node_templates[node.name]?.interfaces?.Standard?.create;
        expect(operation, `node_templates['${node.name}'] has no Standard.create`).toBeTruthy();
        expect(operation.description).toBe(update.description);
        expect(operation.implementation).toBe(update.implementation);
        expect(operation.inputs).toEqual(expect.objectContaining({ [update.input.name]: update.input.value }));
    });
    test('a directive node filter on an instance reaches the TOSCA template', async ({ sdcPage: page, api }) => {
        const { ns } = await seedNetworkVfcs(api);
        const vf = await api.createVf('PwDesignNodeFilter');
        const node = await addInstance(api, vf, ns);
        const vfcProperties: string[] = (await getComponent(api, ns, 'properties')).properties.map((p: any) => p.name);
        const rule = { property: 'nsName', operator: '=', toscaOperator: 'equal', value: 'Test' };
        expect(vfcProperties).toContain(rule.property);

        await gotoComposition(page, vf, [node.name]);
        await selectNode(page, node.name);
        await page.locator('[data-tests-id="detail-tab-directives-node-filter"] button[role="tab"]').click();
        const tab = page.locator('service-dependencies-tab');
        const directives = tab.locator('.select-directives');
        await directives.locator('.ng-input').click();
        await expect(directives.locator('ng-dropdown-panel').getByRole('option'), 'the directive types offered').toHaveText(
            ['select', 'selectable', 'substitute', 'substitutable']);
        await directives.getByRole('option', { name: 'selectable', exact: true }).click();
        await directives.locator('#multi-select-apply').click();
        await expect(tab.locator('.checkbox-label-mark-as-dependent label')).toHaveText('Directive: selectable');
        await tab.locator('[data-tests-id="directive-edit-icon"]').click();
        await directives.locator('.ng-input').click();
        await directives.getByRole('option', { name: 'substitutable', exact: true }).click();
        await directives.locator('#multi-select-apply').click();
        await expect(tab.locator('.checkbox-label-mark-as-dependent label'))
            .toHaveText('Directive: selectable,substitutable');

        const propertiesSection = tab.locator('sdc-accordion').filter({ hasText: 'Node Filter Properties' });
        await propertiesSection.locator('[data-tests-id="add-rule-button"]').click();
        const editor = page.locator('service-dependencies-editor');
        const propertySelect = editor.locator('[data-tests-id="servicePropertyName"] select');
        const offered = (await propertySelect.locator('option').allInnerTexts())
            .map(o => o.trim()).filter(o => !o.startsWith('Select'));
        expect(offered.sort(), 'the rule editor offers other properties than the VFC has').toEqual([...vfcProperties].sort());
        await propertySelect.selectOption({ label: rule.property });
        await editor.locator('[data-tests-id="constraintOperator"] select').selectOption({ label: rule.operator });
        await editor.locator('[data-tests-id="value-type-static"]').click();
        await editor.locator('[data-tests-id="ruleAssignedValue"] input').fill(rule.value);
        const saved = page.waitForResponse((r) => r.request().method() === 'POST'
            && /\/nodeFilter/.test(r.url()), { timeout: 30_000 });
        await page.getByRole('button', { name: 'Create', exact: true }).click();
        expect((await saved).ok(), 'the node filter was rejected').toBe(true);
        await expect(propertiesSection, 'the new rule is not listed').toContainText(rule.property);

        const { componentInstances } = await getComponent(api, vf, 'componentInstances');
        expect(componentInstances.find((i: any) => i.uniqueId === node.id).directives.sort())
            .toEqual(['selectable', 'substitutable']);

        const template = await downloadToscaTemplate(page, vf);
        const nodeTemplate = template.topology_template.node_templates[node.name];
        expect(nodeTemplate?.directives?.sort()).toEqual(['selectable', 'substitutable']);
        const filters: any[] = nodeTemplate?.node_filter?.properties || [];
        const filter = filters.find(f => rule.property in f);
        expect(filter, `node_filter.properties has no ${rule.property}: ${JSON.stringify(filters)}`).toBeTruthy();
        expect(filter[rule.property]).toEqual([{ [rule.toscaOperator]: rule.value }]);
    });
    test('substitution filters on every property type reach the TOSCA template', async ({ sdcPage: page, api }) => {
        const vf = await api.createVf('PwDesignSubFilter');
        await addProperties(api, vf, DESIGN_PROPERTIES);
        const filters: { property: string; fill: (editor: Locator) => Promise<void>; expected: unknown }[] = [
            { property: 'property1', fill: e => e.locator('[data-tests-id="ruleAssignedValue"] input').fill('IntegrationTest'), expected: 'IntegrationTest' },
            { property: 'property2', fill: e => e.locator('[data-tests-id="ruleAssignedValue"] input').fill('202'), expected: 202 },
            { property: 'property3', fill: e => e.locator('[data-tests-id="ruleAssignedValue"] select').selectOption({ label: 'TRUE' }), expected: true },
            {
                property: 'property4', expected: ['value1', 'value2'], fill: async (e) => {
                    for (const [i, v] of ['value1', 'value2'].entries()) {
                        await e.locator('[data-tests-id="add-to-list-property4"]').click();
                        await e.locator(`[data-tests-id="value-prop-property4.${i}"]`).fill(v);
                    }
                },
            },
            {
                property: 'property5', expected: { MyKey: 'MyValue' }, fill: async (e) => {
                    await e.locator('[data-tests-id="add-to-list-property5"]').click();
                    await e.locator('[data-tests-id="value-prop-key-property5.0"]').fill('MyKey');
                    await e.locator('[data-tests-id="value-prop-property5.0"]').fill('MyValue');
                },
            },
            { property: 'property6', fill: e => e.locator('[data-tests-id="ruleAssignedValue"] input').fill('500 GB'), expected: '500 GB' },
        ];

        await gotoComposition(page, vf, []);
        await page.locator('[data-tests-id="detail-tab-substitution-filter"] button[role="tab"]').click();
        const tab = page.locator('substitution-filter-tab');
        for (const filter of filters) {
            await tab.locator('[data-tests-id="add-substitution-filter-button"]').click();
            const editor = page.locator('service-dependencies-editor');
            await editor.locator('[data-tests-id="servicePropertyName"] select').selectOption({ label: filter.property });
            await editor.locator('[data-tests-id="constraintOperator"] select').selectOption({ label: '=' });
            await editor.locator('[data-tests-id="value-type-static"]').click();
            await filter.fill(editor);
            const saved = page.waitForResponse((r) => r.request().method() === 'POST'
                && /\/substitutionFilter/.test(r.url()), { timeout: 30_000 });
            await page.getByRole('button', { name: 'Create', exact: true }).click();
            expect((await saved).ok(), `the ${filter.property} filter was rejected`).toBe(true);
            await expect(editor).toHaveCount(0);
            await expect(tab.locator('.rule-desc').filter({ hasText: filter.property }),
                `the ${filter.property} filter is not listed`).toHaveCount(1);
        }

        const template = await downloadToscaTemplate(page, vf);
        const stored: any[] = template.topology_template.substitution_mappings?.substitution_filter?.properties || [];
        for (const filter of filters) {
            const entry = stored.find(f => filter.property in f);
            expect(entry, `substitution_filter has no ${filter.property}: ${JSON.stringify(stored)}`).toBeTruthy();
            expect(entry[filter.property], filter.property).toEqual([{ equal: filter.expected }]);
        }
    });
    test('renaming an instance from the side panel is stored', async ({ sdcPage: page, api }) => {
        const vf = await api.createVf('PwDesignRename');
        await api.addComputeInstance(vf);
        const before = (await getComponent(api, vf, 'componentInstances')).componentInstances[0];

        await gotoComposition(page, vf, [before.name]);
        await selectNode(page, before.name);
        await page.locator('[data-tests-id="renameInstance"]').click();
        const modal = page.getByRole('dialog', { name: 'Edit Name' });
        await modal.getByRole('textbox', { name: 'Instance Name' }).fill('newName');
        const saved = page.waitForResponse((r) => r.request().method() === 'POST'
            && r.url().endsWith(`/resourceInstance/${before.uniqueId}`), { timeout: 30_000 });
        await modal.getByRole('button', { name: 'OK', exact: true }).click();
        expect((await saved).ok(), 'the rename was rejected').toBe(true);
        await expect(modal).toHaveCount(0);
        await expect(page.locator('[data-tests-id="selectedCompTitle"]')).toHaveText('newName');

        const after = (await getComponent(api, vf, 'componentInstances')).componentInstances;
        expect(after.map((i: any) => [i.uniqueId, i.name])).toEqual([[before.uniqueId, 'newName']]);
    });
});
