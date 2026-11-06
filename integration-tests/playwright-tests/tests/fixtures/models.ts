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
 * API seeding for the model / VFC-import / ETSI specs: VFC imports, lifecycle, instances and
 * relations, TOSCA models and type imports, and VSP onboarding up to a VF.
 */

import * as crypto from 'crypto';
import * as fs from 'fs';
import * as path from 'path';
import JSZip from 'jszip';
import { Page } from '@playwright/test';
import { expect, SdcApi, CreatedAsset, OnboardingItem, ADMIN_USER, uniqueName, gotoWorkspaceTab } from './sdc';
import { Csar, FILES_DIR, downloadCsar } from './csar';

export const DEFAULT_MODEL = 'SDC AID';
export const ETSI_MODEL = 'ETSI SOL001 v2.5.1';

export interface Category {
    category: string;
    subcategory?: string;
}

/** The value of the General tab's category <select> for a resource category. */
export function categoryOption(c: Category): string {
    return c.subcategory ? `${c.category}_#_${c.subcategory}` : c.category;
}

function categoriesBody(c: Category, prefix: 'resourceNewCategory' | 'serviceNewCategory'): any[] {
    const normalized = c.category.toLowerCase();
    const category: any = { name: c.category, normalizedName: normalized, uniqueId: `${prefix}.${normalized}` };
    if (c.subcategory) {
        const sub = c.subcategory.toLowerCase();
        category.subcategories = [{ name: c.subcategory, normalizedName: sub, uniqueId: `${prefix}.${normalized}.${sub}` }];
    }
    return [category];
}

async function expectStatus(resp: { status(): number; text(): Promise<string> }, expected: number, what: string) {
    if (resp.status() !== expected) {
        expect(resp.status(), `${what}: ${(await resp.text()).slice(0, 600)}`).toBe(expected);
    }
}

/**
 * TOSCA type names are unique per model and a certified resource cannot be deleted, so a VFC file
 * imported twice into one stack is rejected (SVC4546). Renames every `org.openecomp.resource.<X>`
 * node type in `text` to `<X>Pw<suffix>`, keeping the scenario and making the type fresh.
 */
export function uniquifyNodeTypes(text: string, suffix: string): string {
    return text.replace(/(org\.openecomp\.resource\.[A-Za-z0-9_-]+)/g, `$1Pw${suffix}`);
}

/** A per-test copy of a VFC file under `Files/VFCs`, with {@link uniquifyNodeTypes} applied. */
export function uniqueVfcFile(fileName: string, suffix: string, outDir: string): { file: string; text: string } {
    const text = uniquifyNodeTypes(fs.readFileSync(path.join(FILES_DIR, 'VFCs', fileName), 'utf8'), suffix);
    fs.mkdirSync(outDir, { recursive: true });
    const file = path.join(outDir, fileName);
    fs.writeFileSync(file, text);
    return { file, text };
}

export function nodeTypeName(yamlText: string): string {
    const m = /^node_types:\s*\n\s+(\S+):/m.exec(yamlText);
    expect(m, 'the file declares no node type').toBeTruthy();
    return m![1];
}

export interface VfcData {
    name: string;
    model?: string;
    category: Category;
    description: string;
    vendorName: string;
    vendorRelease: string;
    vendorModelNumber: string;
}

/** Imports a VFC the way the dashboard's Import VFC does: payload on the create POST, Content-MD5 over the body. */
export async function importVfc(api: SdcApi, data: VfcData, fileName: string, yamlText: string): Promise<CreatedAsset> {
    const body: any = {
        name: data.name,
        description: data.description,
        componentType: 'RESOURCE',
        resourceType: 'VFC',
        categories: categoriesBody(data.category, 'resourceNewCategory'),
        vendorName: data.vendorName,
        vendorRelease: data.vendorRelease,
        resourceVendorModelNumber: data.vendorModelNumber,
        contactId: 'cs0008',
        icon: 'defaulticon',
        tags: [data.name],
        payloadName: fileName,
        payloadData: Buffer.from(yamlText).toString('base64'),
    };
    if (data.model && data.model !== DEFAULT_MODEL) {
        body.model = data.model;
    }
    const json = JSON.stringify(body);
    const md5 = Buffer.from(crypto.createHash('md5').update(json).digest('hex')).toString('base64');
    const resp = await api.postCreate('/catalog/resources', { headers: { 'Content-MD5': md5 }, data: json });
    return api.readable(resp, data.name, 'resource');
}

/** Creates a VF in `model` (the default model when omitted). */
export async function createVfInModel(api: SdcApi, model: string | undefined, namePrefix = 'PwModelVf'): Promise<CreatedAsset> {
    const name = uniqueName(namePrefix);
    const data: any = {
        name,
        description: 'created by the Playwright suite',
        componentType: 'RESOURCE',
        resourceType: 'VF',
        categories: categoriesBody({ category: 'Generic', subcategory: 'Abstract' }, 'resourceNewCategory'),
        vendorName: 'pwVendor',
        vendorRelease: '1.0',
        contactId: 'cs0008',
        icon: 'defaulticon',
        tags: [name],
    };
    if (model && model !== DEFAULT_MODEL) {
        data.model = model;
    }
    const resp = await api.postCreate('/catalog/resources', { data });
    return api.readable(resp, name, 'resource');
}

export async function createServiceInModel(
    api: SdcApi, opts: { model?: string; category: string; namePrefix?: string; extra?: Record<string, unknown> },
): Promise<CreatedAsset> {
    const name = uniqueName(opts.namePrefix || 'PwModelSvc');
    const data: any = {
        name,
        description: 'aDescription',
        componentType: 'SERVICE',
        categories: categoriesBody({ category: opts.category }, 'serviceNewCategory'),
        contactId: 'cs0008',
        icon: 'defaulticon',
        tags: [name],
        ...(opts.extra || {}),
    };
    if (opts.model && opts.model !== DEFAULT_MODEL) {
        data.model = opts.model;
    }
    const resp = await api.postCreate('/catalog/services', { data });
    return api.readable(resp, name, 'service');
}

/** Certifies straight from checkout (CertificationChangeTransition allows it). Returns the 1.0 asset. */
export async function certify(api: SdcApi, asset: CreatedAsset): Promise<CreatedAsset> {
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/lifecycleState/certify`),
        { headers: api.headers, data: { userRemarks: 'certified by the Playwright suite' } });
    await expectStatus(resp, 200, `certifying ${asset.name}`);
    const body = await resp.json();
    expect(body.lifecycleState, `${asset.name} after certify`).toBe('CERTIFIED');
    return { id: body.uniqueId, name: asset.name, type: asset.type };
}

export async function getComponent(api: SdcApi, asset: CreatedAsset, include: string[]): Promise<any> {
    const query = include.map(i => `include=${i}`).join('&');
    const resp = await api.request.get(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/filteredDataByParams?${query}`),
        { headers: api.headers });
    await expectStatus(resp, 200, `reading ${asset.name}`);
    return resp.json();
}

export interface Instance {
    id: string;
    name: string;
}

/** Instantiates `component` (a uniqueId) into `container`. */
export async function addInstance(
    api: SdcApi, container: CreatedAsset, component: { id: string; originType: string; icon?: string },
    pos = { x: 200, y: 200 },
): Promise<Instance> {
    const read = await api.request.get(api.url(`/catalog/resources/${component.id}/filteredDataByParams?include=metadata`),
        { headers: api.headers });
    await expectStatus(read, 200, `reading ${component.id}`);
    const meta = (await read.json()).metadata;
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(container.type)}/${container.id}/resourceInstance`), {
            headers: api.headers,
            data: {
                componentUid: component.id, componentVersion: meta.version, name: meta.name,
                originType: component.originType, icon: meta.icon, posX: String(pos.x), posY: String(pos.y),
            },
        });
    await expectStatus(resp, 201, `instantiating ${meta.name} in ${container.name}`);
    const body = await resp.json();
    return { id: body.uniqueId, name: body.name };
}

/** The uniqueId of a certified normative by name, e.g. `NsVirtualLink`, in `model`. */
export async function normativeId(api: SdcApi, name: string, model?: string): Promise<string> {
    const query = model && model !== DEFAULT_MODEL ? `?model=${encodeURIComponent(model)}` : '';
    const resp = await api.request.get(api.url(`/catalog/resources/resourceName/${name}/resourceVersion/1.0${query}`),
        { headers: api.headers });
    await expectStatus(resp, 200, `normative ${name} 1.0 in ${model || DEFAULT_MODEL}`);
    return (await resp.json()).uniqueId;
}

/**
 * Relates `from`'s requirement to `to`'s capability, finding both definitions on the container's
 * instances the way the composition's link dialog does.
 */
export async function associate(
    api: SdcApi, container: CreatedAsset,
    from: Instance, requirementName: string, to: Instance, capabilityType: string,
): Promise<void> {
    const c = await getComponent(api, container, ['componentInstances']);
    const reqs: any[] = ([] as any[]).concat(...Object.values(
        (c.componentInstances.find((i: any) => i.uniqueId === from.id) || {}).requirements || {}));
    const caps: any[] = ([] as any[]).concat(...Object.values(
        (c.componentInstances.find((i: any) => i.uniqueId === to.id) || {}).capabilities || {}));
    const req = reqs.find(r => r.name === requirementName);
    expect(req, `${from.name} has no requirement ${requirementName}`).toBeTruthy();
    const cap = caps.find(k => k.type === capabilityType);
    expect(cap, `${to.name} has no capability of type ${capabilityType}`).toBeTruthy();
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(container.type)}/${container.id}/resourceInstance/associate`), {
            headers: api.headers,
            data: {
                fromNode: from.id, toNode: to.id,
                // Shaped like catalog-ui's Relationship.setRelationProperties(), including its use of the
                // capability TYPE as the relationship type.
                relationships: [{
                    relation: {
                        requirementOwnerId: req.ownerId, requirement: req.name, requirementUid: req.uniqueId,
                        capabilityOwnerId: cap.ownerId, capability: cap.name, capabilityUid: cap.uniqueId,
                        relationship: { type: cap.type },
                    },
                    capability: cap,
                    requirement: req,
                }],
            },
        });
    await expectStatus(resp, 200, `relating ${from.name}.${requirementName} -> ${to.name}`);
}

// ---------------------------------------------------------------------------
// CSAR
// ---------------------------------------------------------------------------

/** Opens the TOSCA Artifacts tab and downloads the "Tosca Model" CSAR through the UI. */
export async function downloadToscaModelCsar(page: Page, asset: CreatedAsset): Promise<Csar> {
    await gotoWorkspaceTab(page, { id: asset.id, type: asset.type, tab: 'tosca_artifacts' });
    const button = page.locator('[data-tests-id="download_Tosca Model"]');
    await expect(button).toBeVisible({ timeout: 30_000 });
    return downloadCsar(page, () => button.click());
}

/** The `Definitions/<resource|service>-<Systemname>-template.yml` of `name`'s own CSAR. */
export function templatePathFor(type: 'resource' | 'service', name: string): string {
    return `Definitions/${type}-${name.charAt(0).toUpperCase()}${name.slice(1).toLowerCase()}-template.yml`;
}

// ---------------------------------------------------------------------------
// Models and type imports
// ---------------------------------------------------------------------------

async function zipOf(files: Record<string, string | Buffer>): Promise<Buffer> {
    const zip = new JSZip();
    for (const [name, content] of Object.entries(files)) {
        zip.file(name, content);
    }
    return zip.generateAsync({ type: 'nodebuffer' });
}

/**
 * Creates a NORMATIVE model derived from `derivedFrom`. Deriving is what lets a fresh model host a VF:
 * the parent supplies the normative node types, and its imports are added to every CSAR of the model.
 */
export async function createModel(api: SdcApi, derivedFrom: string, namePrefix = 'PwModel'): Promise<string> {
    const name = uniqueName(namePrefix);
    const imports = await zipOf({ [`${name}.yaml`]: 'tosca_definitions_version: tosca_simple_yaml_1_3\n' });
    const resp = await api.request.post(api.url('/catalog/model'), {
        headers: { USER_ID: ADMIN_USER },
        multipart: {
            model: { name: 'model.json', mimeType: 'application/json',
                buffer: Buffer.from(JSON.stringify({ name, derivedFrom, modelType: 'NORMATIVE' })) },
            modelImportsZip: { name: 'imports.zip', mimeType: 'application/zip', buffer: imports },
        },
    });
    await expectStatus(resp, 201, `creating model ${name}`);
    return name;
}

const TOSCA_IMPORTS = path.join(FILES_DIR, 'imports', 'tosca');

/** The uploads ModelToscaTypeImportTest makes, from ToscaTypeUploadEnum. */
const TYPE_UPLOADS = [
    { suffix: 'datatypes', dir: 'data-types', zipParam: 'dataTypesZip', metadata: false },
    { suffix: 'capability', dir: 'capability-types', zipParam: 'capabilityTypeZip', metadata: false },
    { suffix: 'relationship', dir: 'relationship-types', zipParam: 'relationshipTypeZip', metadata: false },
    { suffix: 'interfaceLifecycle', dir: 'interface-lifecycle-types', zipParam: 'interfaceLifecycleTypeZip', metadata: false },
    { suffix: 'grouptypes', dir: 'group-types', zipParam: 'groupTypesZip', metadata: true },
    { suffix: 'policytypes', dir: 'policy-types', zipParam: 'policyTypesZip', metadata: true },
];

function onlyFile(dir: string, ext: string): string {
    const found = fs.readdirSync(dir).filter(f => f.toLowerCase().endsWith(ext));
    expect(found.length, `${dir} should hold exactly one *${ext}`).toBe(1);
    return path.join(dir, found[0]);
}

/**
 * Uploads every type file under `Files/imports/tosca` into `model`; each must answer 201. Without
 * `includeToModelImport` the types land in the model but in none of its imports, so a CSAR that uses
 * one references a type it never defines.
 */
export async function importTypesIntoModel(api: SdcApi, model: string, includeToModelImport = true): Promise<void> {
    for (const t of TYPE_UPLOADS) {
        const dir = path.join(TOSCA_IMPORTS, t.dir);
        const yml = onlyFile(dir, '.yml');
        const multipart: any = {
            model,
            includeToModelImport: String(includeToModelImport),
            [t.zipParam]: { name: `${path.basename(yml)}.zip`, mimeType: 'application/octet-stream',
                buffer: await zipOf({ [path.basename(yml)]: fs.readFileSync(yml) }) },
        };
        if (t.metadata) {
            multipart.toscaTypeMetadata = fs.readFileSync(onlyFile(dir, '.json'), 'utf8');
        }
        const resp = await api.request.post(api.url(`/catalog/uploadType/${t.suffix}`),
            { headers: { USER_ID: 'cs0008' }, multipart });
        await expectStatus(resp, 201, `uploading ${t.suffix} into ${model}`);
    }
}

/** Imports the `Files/imports/tosca/node-types/<dir>` node type into `model`; returns its uniqueId. */
export async function importNodeTypeIntoModel(api: SdcApi, dir: string, model: string): Promise<{ id: string; name: string }> {
    const base = path.join(TOSCA_IMPORTS, 'node-types', dir);
    const yml = onlyFile(base, '.yml');
    const meta = JSON.parse(fs.readFileSync(onlyFile(base, '.json'), 'utf8'));
    meta.model = model;
    const resp = await api.request.post(api.url('/catalog/upload/multipart'), {
        headers: { USER_ID: ADMIN_USER },
        multipart: {
            resourceMetadata: JSON.stringify(meta),
            resourceZip: { name: `${path.basename(yml)}.zip`, mimeType: 'application/octet-stream',
                buffer: await zipOf({ [path.basename(yml)]: fs.readFileSync(yml) }) },
        },
    });
    await expectStatus(resp, 201, `importing node type ${meta.name} into ${model}`);
    return { id: (await resp.json()).uniqueId, name: meta.name };
}

// ---------------------------------------------------------------------------
// Onboarding: VSP with a package, up to a VF
// ---------------------------------------------------------------------------

/** A NetworkPackage VSP for `vlm`, optionally bound to `model`, with `packageFile` uploaded, processed, submitted and packaged. */
export async function onboardVsp(
    api: SdcApi, vlm: OnboardingItem, packageFile: string, opts: { model?: string; namePrefix?: string } = {},
): Promise<OnboardingItem & { description: string; vendorName: string }> {
    const name = uniqueName(opts.namePrefix || 'PwModelVsp');
    const data: any = {
        name,
        description: 'description',
        category: 'resourceNewCategory.network l4+',
        subCategory: 'resourceNewCategory.network l4+.common network resources',
        vendorName: vlm.name,
        vendorId: vlm.id,
        onboardingMethod: 'NetworkPackage',
        icon: 'icon',
        licensingData: {},
    };
    if (opts.model) {
        data.selectedModelList = [opts.model];
    }
    const created = await api.request.post(api.onboardingUrl('/vendor-software-products'), { headers: api.headers, data });
    const vsp = await api.onboardingItem(created, name, 'vendor-software-products');
    const base = `/vendor-software-products/${vsp.id}/versions/${vsp.versionId}`;

    const upload = await api.request.post(api.onboardingUrl(`${base}/orchestration-template-candidate`), {
        headers: { USER_ID: 'cs0008' },
        multipart: { upload: { name: path.basename(packageFile), mimeType: 'application/octet-stream',
            buffer: fs.readFileSync(packageFile) } },
    });
    await expectStatus(upload, 200, `uploading ${path.basename(packageFile)} to ${name}`);
    const uploadBody = await upload.json();
    expect(uploadBody.status, `upload of ${path.basename(packageFile)}: ${JSON.stringify(uploadBody).slice(0, 600)}`)
        .toBe('Success');

    const processed = await api.request.put(api.onboardingUrl(`${base}/orchestration-template-candidate/process`),
        { headers: api.headers, data: {} });
    await expectStatus(processed, 200, `processing the package of ${name}`);
    const processBody = await processed.json();
    expect(processBody.status, `processing ${name}: ${JSON.stringify(processBody).slice(0, 600)}`).toBe('Success');

    for (const action of ['Submit', 'Create_Package']) {
        const resp = await api.request.put(api.onboardingUrl(`${base}/actions`), {
            headers: api.headers,
            data: action === 'Submit' ? { action, submitRequest: { message: 'Submit' } } : { action },
        });
        await expectStatus(resp, 200, `${action} of ${name}`);
    }
    return { ...vsp, description: 'description', vendorName: vlm.name };
}

/** Creates the VF of a packaged VSP, as Import VSP + Create does. */
export async function createVfFromVsp(
    api: SdcApi, vsp: OnboardingItem & { description: string; vendorName: string }, model?: string,
): Promise<CreatedAsset> {
    const data: any = {
        name: vsp.name,
        description: vsp.description,
        componentType: 'RESOURCE',
        resourceType: 'VF',
        categories: categoriesBody({ category: 'Network L4+', subcategory: 'Common Network Resources' }, 'resourceNewCategory'),
        vendorName: vsp.vendorName,
        vendorRelease: '1.0',
        resourceVendorModelNumber: '',
        contactId: 'cs0008',
        icon: 'defaulticon',
        tags: [vsp.name],
        csarUUID: vsp.id,
        csarVersion: '1.0',
        csarVersionId: vsp.versionId,
    };
    if (model && model !== DEFAULT_MODEL) {
        data.model = model;
    }
    const resp = await api.postCreate('/catalog/resources', { data, timeout: 120_000 });
    return api.readable(resp, vsp.name, 'resource');
}

/**
 * The raw bytes of a CSAR entry, for packages nested in a CSAR (the NSD in Artifacts/ETSI_PACKAGE).
 * `Csar.text()` decodes UTF-8 and would corrupt them.
 */
