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

import { Locator, Page } from '@playwright/test';
import { test, expect, SEL, SdcApi, CreatedAsset, uniqueName, gotoWorkspaceTab, loaderGone } from './fixtures/sdc';
import { Csar } from './fixtures/csar';
import * as models from './fixtures/models';

const ROOT_FILE = 'org.openecomp.resource.VFC-root.yml';
const CHILD_FILE = 'org.openecomp.resource.VFC-child.yml';

const VARIANTS = [
    { label: 'the default model', model: models.DEFAULT_MODEL,
        category: { category: 'Network L4+', subcategory: 'Common Network Resources' } },
    { label: models.ETSI_MODEL, model: models.ETSI_MODEL,
        category: { category: 'Application L4+', subcategory: 'Database' } },
];

function vfcData(model: string, category: models.Category, prefix: string): models.VfcData {
    return {
        name: uniqueName(prefix), model, category,
        description: 'aDescription', vendorName: 'Ericsson', vendorRelease: '1.2.3', vendorModelNumber: '4.5.6',
    };
}

/** Imports and certifies the root VFC, then imports the child derived from it. Returns the child (0.1, checked out). */
async function seedChildVfc(api: SdcApi, model: string, category: models.Category, outDir: string) {
    const suffix = uniqueName('');
    const root = models.uniqueVfcFile(ROOT_FILE, suffix, outDir);
    const child = models.uniqueVfcFile(CHILD_FILE, suffix, outDir);
    await models.certify(api, await models.importVfc(api, vfcData(model, category, 'PwVfcRoot'), ROOT_FILE, root.text));
    const data = vfcData(model, category, 'PwVfcChild');
    const asset = await models.importVfc(api, data, CHILD_FILE, child.text);
    return { asset, data, toscaName: models.nodeTypeName(child.text) };
}

function checkMetadata(template: any, data: models.VfcData) {
    const metadata = template.metadata;
    expect(metadata.name).toBe(data.name);
    expect(metadata.description).toBe(data.description);
    expect(metadata.category).toBe(data.category.category);
    expect(metadata.subcategory).toBe(data.category.subcategory);
    expect(metadata.type).toBe('VFC');
    expect(metadata.resourceVendor).toBe(data.vendorName);
    expect(metadata.resourceVendorRelease).toBe(data.vendorRelease);
    expect(metadata.reourceVendorModelNumber).toBe(data.vendorModelNumber);
}

async function vfcTemplate(csar: Csar, name: string): Promise<any> {
    expect(await csar.mainTemplatePath()).toBe(models.templatePathFor('resource', name));
    return csar.mainTemplate();
}

function checkNodeType(template: any, toscaName: string): any {
    expect(Object.keys(template.node_types)).toEqual([toscaName]);
    const nodeType = template.node_types[toscaName];
    expect(nodeType.description).toBe('aDescription');
    expect(Object.keys(nodeType.properties || {})).toContain('aProperty');
    expect(Object.keys(nodeType.attributes || {}).length).toBeGreaterThan(0);
    expect(Object.keys(nodeType.interfaces || {}).length).toBeGreaterThan(0);
    return nodeType;
}

async function openImportVfc(page: Page, file: string): Promise<void> {
    await page.locator('[data-tests-id="importButtonsArea"]').hover();
    await page.locator('[data-tests-id="fileimportVFCbutton"]').setInputFiles(file);
    await expect(page.locator(SEL.name)).toBeVisible({ timeout: 30_000 });
}

async function fillVfcForm(page: Page, data: models.VfcData): Promise<void> {
    await page.locator(SEL.name).fill(data.name);
    if (data.model !== models.DEFAULT_MODEL) {
        await page.locator('[data-tests-id="selectModelName"]').selectOption({ label: data.model! });
    }
    await page.locator(SEL.category).selectOption(models.categoryOption(data.category));
    await page.locator('[data-tests-id="i-sdc-tag-input"]').fill('importVFC');
    await page.locator('[data-tests-id="i-sdc-tag-input"]').press('Enter');
    await page.locator(SEL.description).fill(data.description);
    await page.locator(SEL.vendorName).fill(data.vendorName);
    await page.locator(SEL.vendorRelease).fill(data.vendorRelease);
    await page.locator('[data-tests-id="resourceVendorModelNumber"]').fill(data.vendorModelNumber);
}

async function createFromForm(page: Page, data: models.VfcData): Promise<CreatedAsset> {
    const created = page.waitForResponse(r => r.request().method() === 'POST' && /\/v1\/catalog\/resources$/.test(r.url()),
        { timeout: 60_000 });
    await page.locator(SEL.chromeCreateButton).click();
    const resp = await created;
    const body = await resp.json();
    expect(resp.status(), JSON.stringify(body).slice(0, 400)).toBe(201);
    await expect(page.locator('[data-tests-id="formlifecyclestate"]')).toBeVisible({ timeout: 30_000 });
    return { id: body.uniqueId, name: data.name, type: 'resource' };
}

/** The ng2 custom modal holding the operation handler (composition and Interfaces tab alike). */
function operationModal(page: Page): Locator {
    return page.locator('.ng2-modal-content, .custom-modal').filter({ has: page.locator('operation-handler') });
}

async function selectCanvasNode(page: Page, name: string): Promise<void> {
    const canvas = page.locator('.sdc-composition-graph-wrapper');
    await expect(canvas).toBeVisible({ timeout: 30_000 });
    const position = await expect.poll(() => page.evaluate((n) => {
        const cy = (window as any).jQuery('.sdc-composition-graph-wrapper').cytoscape('get');
        const node = cy && cy.nodes(`[name="${n}"]`);
        return node && node.length ? node.renderedPosition() : null;
    }, name), { timeout: 30_000, message: `${name} never rendered on the canvas` }).not.toBeNull()
        .then(() => page.evaluate((n) => {
            const cy = (window as any).jQuery('.sdc-composition-graph-wrapper').cytoscape('get');
            return cy.nodes(`[name="${n}"]`).renderedPosition();
        }, name));
    const box = (await canvas.boundingBox())!;
    await page.mouse.click(box.x + position.x, box.y + position.y);
    await expect(page.locator('[data-tests-id="selectedCompTitle"]')).toHaveText(name, { timeout: 30_000 });
}

test.describe('Models: Import VFC', () => {

    for (const v of VARIANTS) {
        test(`imports a VFC into ${v.label} and its certified CSAR carries the metadata and node type`, async ({ sdcPage: page, api }, testInfo) => {
            test.setTimeout(120_000);
            const root = models.uniqueVfcFile(ROOT_FILE, uniqueName(''), testInfo.outputPath());
            const data = vfcData(v.model, v.category, 'PwVfcRoot');

            await openImportVfc(page, root.file);
            await fillVfcForm(page, data);
            const vfc = await createFromForm(page, data);

            const stored = (await models.getComponent(api, vfc, ['metadata'])).metadata;
            expect(stored.resourceType).toBe('VFC');
            expect(stored.toscaResourceName).toBe(models.nodeTypeName(root.text));
            expect(stored.model || models.DEFAULT_MODEL).toBe(v.model);
            expect(stored.tags).toEqual(expect.arrayContaining([data.name, 'importVFC']));

            const certified = await models.certify(api, vfc);
            const template = await vfcTemplate(await models.downloadToscaModelCsar(page, certified), data.name);
            checkMetadata(template, data);
            checkNodeType(template, models.nodeTypeName(root.text));
        });

        test(`manages the attributes of an imported child VFC in ${v.label}`, async ({ sdcPage: page, api }, testInfo) => {
            test.setTimeout(120_000);
            const { asset, data, toscaName } = await seedChildVfc(api, v.model, v.category, testInfo.outputPath());

            await gotoWorkspaceTab(page, { id: asset.id, type: 'resource', tab: 'attributes' });
            await loaderGone(page);
            const row = (name: string) => page.locator(`[data-tests-id="attrib-name_${name}"]`);
            for (const name of ['test_1', 'test_2', 'test_3', 'test_4']) {
                await expect(row(name)).toBeVisible({ timeout: 30_000 });
            }

            await page.getByRole('button', { name: 'Delete test_2', exact: true }).click();
            const confirm = page.locator('.sdc-modal').filter({ hasText: 'test_2' });
            await confirm.getByRole('button', { name: 'OK' }).click();
            await expect(row('test_2')).toHaveCount(0);

            const modal = page.locator('.sdc-modal').filter({ has: page.locator('attribute-modal') });
            await page.locator('[data-tests-id="add-attribute-button"]').click();
            await modal.locator('[data-tests-id="attributeName"]').fill('test_9');
            await modal.locator('[data-tests-id="description"]').fill('Additional attribute added from UI');
            await modal.getByRole('combobox', { name: 'Type' }).click();
            await modal.locator('li[data-tests-id="string"]').click();
            await modal.locator('[data-tests-id="defaultValue"]').fill('one More Attribute');
            await modal.locator('[data-tests-id="button-save"]').click();
            await expect(modal).toHaveCount(0);
            await expect(row('test_9')).toBeVisible();

            await page.getByRole('button', { name: 'Edit test_9', exact: true }).click();
            await expect(modal.locator('[data-tests-id="attributeName"]')).toHaveValue('test_9');
            await modal.locator('[data-tests-id="description"]').fill('ADDITIONAL ATTRIBUTE ADDED FROM UI');
            await modal.locator('[data-tests-id="defaultValue"]').fill('ONE MORE ATTRIBUTE');
            await modal.locator('[data-tests-id="button-save"]').click();
            await expect(modal).toHaveCount(0);
            await expect(row('test_9')).toBeVisible();

            await expect.poll(async () => ((await models.getComponent(api, asset, ['attributes'])).attributes || [])
                .map((a: any) => `${a.name}|${a.type}|${a.description || ''}|${a._default ?? a.defaultValue ?? ''}`).sort(),
            { timeout: 15_000 }).toEqual(expect.arrayContaining([
                'test_9|string|ADDITIONAL ATTRIBUTE ADDED FROM UI|ONE MORE ATTRIBUTE',
            ]));
            const names = ((await models.getComponent(api, asset, ['attributes'])).attributes || []).map((a: any) => a.name);
            expect(names).toEqual(expect.arrayContaining(['test_1', 'test_3', 'test_4', 'test_9']));
            expect(names).not.toContain('test_2');

            const certified = await models.certify(api, asset);
            const template = await vfcTemplate(await models.downloadToscaModelCsar(page, certified), data.name);
            checkMetadata(template, data);
            const nodeType = checkNodeType(template, toscaName);
            expect(Object.keys(nodeType.attributes)).toEqual(expect.arrayContaining(['test_1', 'test_3', 'test_4', 'test_9']));
            expect(Object.keys(nodeType.attributes)).not.toContain('test_2');
            expect(nodeType.attributes.test_9).toMatchObject({
                type: 'string', description: 'ADDITIONAL ATTRIBUTE ADDED FROM UI', default: 'ONE MORE ATTRIBUTE',
            });
        });

        test(`shows the create operation of an imported child VFC in ${v.label}`, async ({ sdcPage: page, api }, testInfo) => {
            test.setTimeout(120_000);
            const { asset } = await seedChildVfc(api, v.model, v.category, testInfo.outputPath());

            await gotoWorkspaceTab(page, { id: asset.id, type: 'resource', tab: 'interfaceDefinition' });
            await loaderGone(page);
            await page.locator('interface-definition').getByRole('button', { name: 'create', exact: true }).click();
            const modal = operationModal(page);
            await expect(modal.locator('[data-tests-id="interface-operation-description"]')).toHaveValue('My create Desc');
            await expect(modal.locator('[data-tests-id="interface-operation-implementation-name"]'))
                .toHaveValue('path/to/my/implementation.sh');
            await expect(modal.locator('.input-tree .input-label')).toHaveText(['first:']);
            await modal.locator('[data-tests-id="Cancel"]').click();
            await expect(modal).toHaveCount(0);
        });

        test(`edits the create operation of a child VFC instance in a VF, from ${v.label}`, async ({ sdcPage: page, api }, testInfo) => {
            test.setTimeout(150_000);
            const { asset } = await seedChildVfc(api, v.model, v.category, testInfo.outputPath());
            const vfc = await models.certify(api, asset);
            const vf = await models.createVfInModel(api, models.DEFAULT_MODEL, 'PwVfcHostVf');
            const instance = await models.addInstance(api, vf, { id: vfc.id, originType: 'VFC' });

            await gotoWorkspaceTab(page, { id: vf.id, type: 'resource', tab: 'composition' });
            await loaderGone(page);
            await selectCanvasNode(page, instance.name);
            await page.locator('[data-tests-id="detail-tab-interface-operations"]').click();
            const tab = page.locator('.interface-operations');
            await tab.getByRole('button', { name: 'create', exact: true }).click();

            const modal = operationModal(page);
            await expect(modal.locator('.input-tree .input-label')).toHaveText(['first:']);
            await modal.getByRole('button', { name: 'Delete first' }).click();
            await expect(modal.locator('.input-tree .input-label')).toHaveCount(0);
            await modal.locator('[data-tests-id="interface-operation-description"]').fill('This is CREATE operation');
            await modal.locator('[data-tests-id="interface-operation-implementation-name"]').fill('fullPath/to/my/newImplementation.sh');

            const inputs = [
                { name: 'stringInput', type: 'string', value: '1' },
                { name: 'booleanInput', type: 'boolean', value: 'true' },
                { name: 'integerInput', type: 'integer', value: '1' },
            ];
            for (const input of inputs) {
                await modal.locator('[data-tests-id="add-input.add-input-link"]').click();
                await modal.locator('[data-tests-id="add-input.input-name"]').fill(input.name);
                const type = modal.locator('input[data-tests-id^="add-input.input-type"]');
                await type.click();
                await type.pressSequentially(input.type);
                await modal.locator(`dropdown-results li[data-tests-id="${input.type}"]`).click();
                await modal.locator('[data-tests-id="add-input.add-input-btn"]').click();
                const item = modal.locator('app-input-list-item').filter({ has: page.locator('.input-label', { hasText: `${input.name}:` }) });
                await item.getByRole('button', { name: input.name, exact: true }).click();
                const field = item.locator('.input-value input, .input-value select').first();
                if (input.type === 'boolean') {
                    await field.selectOption(input.value);
                } else {
                    await field.fill(input.value);
                }
            }
            const saved = page.waitForResponse(r => r.request().method() === 'PUT' && /interfaceOperation/i.test(r.url()));
            await modal.getByRole('button', { name: 'Save', exact: true }).click();
            expect((await saved).ok()).toBe(true);
            await expect(modal).toHaveCount(0);

            await expect(tab.locator('.data-row .field-description')).toContainText('This is CREATE operation');
            await page.locator('[data-tests-id="detail-tab-information"]').click();
            await page.locator('[data-tests-id="detail-tab-interface-operations"]').click();
            await tab.getByRole('button', { name: 'create', exact: true }).click();
            await expect(modal.locator('[data-tests-id="interface-operation-description"]')).toHaveValue('This is CREATE operation');
            await expect(modal.locator('[data-tests-id="interface-operation-implementation-name"]'))
                .toHaveValue('fullPath/to/my/newImplementation.sh');
            await expect(modal.locator('.input-tree .input-label')).toHaveText(inputs.map(i => `${i.name}:`));
            await modal.getByRole('button', { name: 'Cancel', exact: true }).click();
            await expect(modal).toHaveCount(0);

            const certified = await models.certify(api, vf);
            const csar = await models.downloadToscaModelCsar(page, certified);
            const topology = (await csar.mainTemplate()).topology_template;
            expect(Object.keys(topology.inputs || {}).length).toBeGreaterThan(0);
            expect(Object.keys(topology.substitution_mappings || {}).length).toBeGreaterThan(0);
            const node = topology.node_templates[instance.name];
            expect(node, `the CSAR should hold the ${instance.name} node template`).toBeTruthy();
            expect(node.attributes || {}).toEqual({});
            expect(node.interfaces.Standard.create).toEqual({
                description: 'This is CREATE operation',
                implementation: 'fullPath/to/my/newImplementation.sh',
                inputs: { stringInput: '1', booleanInput: true, integerInput: 1 },
            });
        });
    }

    test(`imports a VFC with the ${models.ETSI_MODEL} model and certifies it from the workspace`, async ({ sdcPage: page, api }, testInfo) => {
        test.setTimeout(120_000);
        const file = models.uniqueVfcFile('VFC-For-Model.yaml', uniqueName(''), testInfo.outputPath());
        const data = vfcData(models.ETSI_MODEL, VARIANTS[0].category, 'PwVfcForModel');

        await openImportVfc(page, file.file);
        await fillVfcForm(page, data);
        const vfc = await createFromForm(page, data);

        await page.locator('[data-tests-id="certify"]').click();
        await page.locator('[data-tests-id="checkindialog"]').fill('Certifying for the UI Integration Test');
        const certified = page.waitForResponse(r => r.request().method() === 'POST' && /lifecycleState\/certify$/.test(r.url()));
        await page.locator('[data-tests-id="confirm-modal-button-ok"]').click();
        const resp = await certified;
        expect(resp.status()).toBe(200);
        const body = await resp.json();
        expect(body.lifecycleState).toBe('CERTIFIED');

        const stored = (await models.getComponent(api, { ...vfc, id: body.uniqueId }, ['metadata'])).metadata;
        expect(stored.lifecycleState).toBe('CERTIFIED');
        expect(stored.version).toBe('1.0');
        expect(stored.model).toBe(models.ETSI_MODEL);
        expect(stored.toscaResourceName).toBe(models.nodeTypeName(file.text));
    });
});
