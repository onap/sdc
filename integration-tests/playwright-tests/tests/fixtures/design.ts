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
 * Seeding for the service-template design specs: imported VFCs, VF compositions, properties.
 *
 *     const { nf, ns } = await seedNetworkVfcs(api);
 *     const vf = await api.createVf('PwDesign');
 *     const nsInstance = await addInstance(api, vf, ns);
 */

import * as crypto from 'crypto';
import * as fs from 'fs';
import { APIResponse, Page } from '@playwright/test';
import { expect, SdcApi, CreatedAsset, uniqueName, loaderGone, gotoWorkspaceTab } from './sdc';
import { Csar, downloadCsar, fixtureFile } from './csar';

export const NETWORK_FUNCTION_VFC = 'VFCs/1-VFC-NetworkFunction.yaml';
export const NETWORK_SERVICE_VFC = 'VFCs/2-VFC-NetworkService.yaml';

export interface Vfc extends CreatedAsset {
    toscaResourceName: string;
}

export interface Instance {
    id: string;
    name: string;
}

async function ok(resp: APIResponse, what: string, status = 200): Promise<any> {
    if (resp.status() !== status) {
        expect(resp.status(), `${what} failed: ${(await resp.text()).slice(0, 600)}`).toBe(status);
    }
    return resp.json().catch(() => ({}));
}

/**
 * A VFC package from the shared Files directory with its node type names made unique.
 *
 * The backend keys an imported VFC on the node type name, not on the resource name, and refuses a
 * second import of the same type (SVC4546). The packages' `MyNetworkFunctionCI` /
 * `MyNetworkServiceCI` types could therefore be imported once per stack, and the NetworkService
 * package derives from the NetworkFunction type, so both are renamed together.
 */
export function vfcPackage(relative: string, suffix: string): { name: string; text: string } {
    const text = fs.readFileSync(fixtureFile(relative), 'utf8')
        .replace(/\b(MyNetworkFunctionCI|MyNetworkServiceCI)\b/g, `$1${suffix}`);
    return { name: relative.split('/').pop()!, text };
}

/** The suffix `seedNetworkVfcs` appended, so a spec can predict the derived node type names. */
export function typeSuffix(): string {
    return uniqueName('').replace(/^\d+/, (d) => d.slice(-6));
}

/** What the Import VFC form posts: the create body plus a Content-MD5 the backend verifies. */
export async function importVfc(api: SdcApi, relative: string, suffix: string, namePrefix: string): Promise<Vfc> {
    const pkg = vfcPackage(relative, suffix);
    const name = uniqueName(namePrefix);
    const body = JSON.stringify({
        name,
        description: 'created by the Playwright suite',
        componentType: 'RESOURCE',
        resourceType: 'VFC',
        categories: [{
            name: 'Generic', normalizedName: 'generic', uniqueId: 'resourceNewCategory.generic',
            subcategories: [{
                name: 'Network Elements', normalizedName: 'network elements',
                uniqueId: 'resourceNewCategory.generic.network elements',
            }],
        }],
        vendorName: 'EST',
        vendorRelease: '4.1.1',
        resourceVendorModelNumber: '0001',
        contactId: 'cs0008',
        icon: 'defaulticon',
        tags: [name],
        payloadName: pkg.name,
        payloadData: Buffer.from(pkg.text).toString('base64'),
    });
    const md5 = Buffer.from(crypto.createHash('md5').update(body).digest('hex')).toString('base64');
    const resp = await api.postCreate('/catalog/resources', { headers: { 'Content-MD5': md5 }, data: body });
    const asset = await api.readable(resp, name, 'resource');
    return { ...asset, toscaResourceName: (await resp.json()).toscaResourceName };
}

/** Checks the asset in and certifies it; returns the certified version (a NEW unique id). */
export async function certify<T extends CreatedAsset>(api: SdcApi, asset: T): Promise<T> {
    const collection = SdcApi.collectionOf(asset.type);
    let body: any;
    for (const action of ['checkin', 'certify']) {
        body = await ok(await api.request.post(
            api.url(`/catalog/${collection}/${asset.id}/lifecycleState/${action}`),
            { headers: api.headers, data: { userRemarks: 'created by the Playwright suite' } }),
        `${action} of ${asset.name}`);
    }
    expect(body.lifecycleState, `${asset.name} did not certify`).toBe('CERTIFIED');
    return { ...asset, id: body.uniqueId };
}

/**
 * The two certified VFCs the design scenarios compose: NetworkFunction (attr_1..3, resourceSubtype)
 * and NetworkService, which derives from it and adds the Standard interface's create/delete.
 */
export async function seedNetworkVfcs(api: SdcApi): Promise<{ nf: Vfc; ns: Vfc; suffix: string }> {
    const suffix = typeSuffix();
    const nf = await certify(api, await importVfc(api, NETWORK_FUNCTION_VFC, suffix, 'PwNF'));
    const ns = await certify(api, await importVfc(api, NETWORK_SERVICE_VFC, suffix, 'PwNS'));
    return { nf, ns, suffix };
}

/** Adds an instance of `child` to `container`; the backend names it `<child name> 0`. */
export async function addInstance(api: SdcApi, container: CreatedAsset, child: CreatedAsset,
                                  pos = { x: 200, y: 200 }): Promise<Instance> {
    const body = await ok(await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(container.type)}/${container.id}/resourceInstance`), {
            headers: api.headers,
            data: {
                componentUid: child.id, componentVersion: '1.0', name: child.name,
                originType: 'VFC', icon: 'defaulticon', posX: String(pos.x), posY: String(pos.y),
            },
        }), `adding ${child.name} to ${container.name}`, 201);
    return { id: body.uniqueId, name: body.name };
}

/**
 * Adds component-level properties, `{ name: type }`, as the Properties Assignment "Add Property" form does;
 * lists and maps get string entries, the schema AddPropertyModal picks.
 */
export async function addProperties(api: SdcApi, asset: CreatedAsset, props: Record<string, string>): Promise<void> {
    for (const [name, type] of Object.entries(props)) {
        await ok(await api.request.post(api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/properties`), {
            headers: api.headers,
            data: {
                [name]: {
                    name, type, required: false, isPassword: false, defaultValue: null,
                    ...(type === 'list' || type === 'map' ? { schema: { property: { type: 'string' } } } : {}),
                },
            },
        }), `adding property ${name} to ${asset.name}`);
    }
}

/**
 * Adds a component input the way the Inputs tab's "Add Input" form does. The form posts to the
 * services collection whatever the component type, and the backend accepts that for a VF.
 */
export async function addInput(api: SdcApi, asset: CreatedAsset, name: string, type = 'string'): Promise<void> {
    await ok(await api.request.post(api.url(`/catalog/services/${asset.id}/create/input`), {
        headers: api.headers,
        data: { [name]: { name, type, required: false, isPassword: false, schema: { property: {} } } },
    }), `adding input ${name} to ${asset.name}`);
}

/** A Service like SdcApi.createService, carrying `extraTags` besides its name tag (ElementFactory's default service). */
export async function createTaggedService(api: SdcApi, namePrefix: string, extraTags: string[]): Promise<CreatedAsset> {
    const name = uniqueName(namePrefix);
    const resp = await api.postCreate('/catalog/services', {
        data: {
            name,
            description: 'service Description',
            componentType: 'SERVICE',
            categories: [{ name: 'Mobility', normalizedName: 'mobility', uniqueId: 'serviceNewCategory.mobility' }],
            contactId: 'al1976',
            icon: 'defaulticon',
            instantiationType: 'A-la-carte',
            tags: [...extraTags, name],
        },
    });
    return api.readable(resp, name, 'service');
}

/** `filteredDataByParams` for the given includes (properties, inputs, outputs, componentInstances, …). */
export async function getComponent(api: SdcApi, asset: CreatedAsset, ...include: string[]): Promise<any> {
    const query = include.map(i => `include=${i}`).join('&');
    return ok(await api.request.get(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/filteredDataByParams?${query}`),
        { headers: api.headers }), `reading ${asset.name}`);
}

/**
 * A resource's own template in a CSAR: `Definitions/resource-<name>-template.yml`, or with
 * `-template-interface.yml` for its node type. The backend capitalises the normalized name, so the
 * entry is matched without regard to case, as the Selenium checks did.
 */
export async function resourceTemplate(csar: Csar, asset: CreatedAsset, kind: 'template' | 'interface' = 'template'): Promise<any> {
    const wanted = `definitions/resource-${asset.name.replace(/[-\s]/g, '').toLowerCase()}-template${kind === 'interface' ? '-interface' : ''}.yml`;
    const entry = csar.entries().find(e => e.toLowerCase() === wanted);
    expect(entry, `${csar.fileName} has no ${wanted}; entries: ${csar.entries().join(', ')}`).toBeTruthy();
    return csar.yaml(entry!);
}

/** Opens the TOSCA Artifacts tab and downloads the TOSCA CSAR, the way DownloadCsarArtifactFlow does. */
export async function downloadToscaCsar(page: Page, asset: CreatedAsset): Promise<Csar> {
    await gotoWorkspaceTab(page, { id: asset.id, type: asset.type, tab: 'tosca_artifacts' });
    await loaderGone(page);
    const download = page.locator('[data-tests-id="download_Tosca Model"]');
    await expect(download).toBeVisible({ timeout: 30_000 });
    return downloadCsar(page, () => download.click());
}

/** The TOSCA template (yml) download on the same tab — DownloadToscaTemplateFlow's artifact. */
export async function downloadToscaTemplate(page: Page, asset: CreatedAsset): Promise<any> {
    await gotoWorkspaceTab(page, { id: asset.id, type: asset.type, tab: 'tosca_artifacts' });
    await loaderGone(page);
    const download = page.locator('[data-tests-id="download_Tosca Template"]');
    await expect(download).toBeVisible({ timeout: 30_000 });
    const [file] = await Promise.all([page.waitForEvent('download', { timeout: 60_000 }), download.click()]);
    expect(await file.failure(), `download of ${file.suggestedFilename()} failed`).toBeNull();
    const { parse } = await import('yaml');
    return parse(fs.readFileSync(await file.path(), 'utf8'));
}

// ---------------------------------------------------------------------------
// Composition canvas
// ---------------------------------------------------------------------------

/** Must match CytoscapeEdgeEditation.HANDLE_SIZE in catalog-ui. */
const HANDLE_SIZE = 18;

/**
 * Opens the Composition tab and returns once the canvas can take input. The global load overlay is
 * painted over the canvas until loadCompositionData() resolves, and the graph is laid out only then,
 * so node positions read before it is gone are wrong.
 */
export async function gotoComposition(page: Page, asset: CreatedAsset, nodes: string[] = []): Promise<void> {
    await gotoWorkspaceTab(page, { id: asset.id, type: asset.type, tab: 'composition' });
    await expect(page.locator('.workspace-composition-page')).toBeAttached({ timeout: 30_000 });
    await loaderGone(page);
    await expect.poll(() => page.evaluate(() => {
        const $ = (window as any).jQuery;
        const cy = $ && $('.sdc-composition-graph-wrapper').cytoscape('get');
        return cy ? cy.nodes().map((n: any) => n.data('name')).sort() : [];
    }), { timeout: 30_000, message: 'the composition graph never rendered the seeded instances' })
        .toEqual([...nodes].sort());
}

/** A node's (or its link handle's) position in page coordinates, from cytoscape's own geometry. */
async function nodePoint(page: Page, name: string, handle = false): Promise<{ x: number; y: number }> {
    const rel = await page.evaluate(({ name, handle, handleSize }) => {
        const cy = (window as any).jQuery('.sdc-composition-graph-wrapper').cytoscape('get');
        const n = cy.nodes(`[name="${name}"]`);
        if (!n.length) { return null; }
        const p = n.renderedPosition();
        return handle
            ? { x: p.x + n.renderedWidth() / 2 - handleSize * cy.zoom(), y: p.y - n.renderedHeight() / 2 }
            : p;
    }, { name, handle, handleSize: HANDLE_SIZE });
    expect(rel, `no node named '${name}' on the canvas`).toBeTruthy();
    const box = await page.locator('.sdc-composition-graph-wrapper').boundingBox();
    return { x: box!.x + rel!.x, y: box!.y + rel!.y };
}

/**
 * Clicks a node and waits for the side panel to show it. The position is re-read on every attempt:
 * the graph may still be animating its initial fit when the overlay goes.
 */
export async function selectNode(page: Page, name: string): Promise<void> {
    await expect(async () => {
        const p = await nodePoint(page, name);
        await page.mouse.click(p.x, p.y);
        await expect(page.locator('[data-tests-id="selectedCompTitle"]')).toHaveText(name, { timeout: 2_000 });
    }).toPass({ timeout: 20_000 });
}

/**
 * Drags a link from `from`'s green-plus handle onto `to`, which opens the relationship wizard.
 *
 * The edge-editation extension arms the handle only in its mousemove hit test (its mousedown has
 * its own hit test commented out), and the first move into a node hit-tests with nothing hovered,
 * so the pointer must move twice and the press may only follow once the extension has switched
 * the body cursor to `pointer` — the one observable sign that the handle is armed.
 */
export async function dragLink(page: Page, from: string, to: string): Promise<void> {
    const target = await nodePoint(page, to);
    const wizard = page.locator('multi-steps-wizard select-requirement-or-capability');
    for (let attempt = 1; attempt <= 3; attempt++) {
        const h = await nodePoint(page, from, true);
        const aim = { x: h.x + HANDLE_SIZE / 4, y: h.y + HANDLE_SIZE / 4 };
        await page.evaluate(() => (window as any).jQuery('body').css('cursor', 'inherit'));
        await page.mouse.move(aim.x + attempt, aim.y + attempt);
        await page.mouse.move(aim.x, aim.y);
        const armed = await expect.poll(
            () => page.evaluate(() => (window as any).jQuery('body').css('cursor')), { timeout: 5_000 })
            .toBe('pointer').then(() => true, () => false);
        if (!armed) { continue; }
        await page.mouse.down();
        await page.mouse.move((aim.x + target.x) / 2, (aim.y + target.y) / 2, { steps: 5 });
        await page.mouse.move(target.x, target.y, { steps: 5 });
        await page.mouse.up();
        if (await wizard.waitFor({ state: 'visible', timeout: 5_000 }).then(() => true, () => false)) {
            return;
        }
    }
    throw new Error(`could not drag a link from '${from}' to '${to}' in 3 attempts`);
}
