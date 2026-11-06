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
 * CATALOG page helpers. The catalog is shared and keeps growing, so every assertion is about a
 * tile the test seeded itself (narrowed with the search box), never about counts of the whole list.
 */

import * as fs from 'fs';
import * as path from 'path';
import { APIResponse, Page } from '@playwright/test';
import { expect, gotoCold, CreatedAsset, SdcApi, DESIGNER_USER, uniqueName, OnboardingItem } from './sdc';
import { fixtureFile } from './csar';

export const CAT = {
    search: '[data-tests-id="main-menu-input-search"]',
    tile: '.sdc-tile',
    header: '.w-sdc-dashboard-catalog-items-header',
    sortByUpdate: '[data-tests-id="sort-by-last-update"]',
    sortAlphabetical: '[data-tests-id="sort-by-alphabetical"]',
    selectorToggle: '.i-sdc-designer-leftbar-section-left-switch-header-text',
    selectorItem: '.sdc-catalog-selector-item',
    /**
     * Checklist labels: `checklist-<section>-<label lower-cased, spaces as dashes>`. Scoped to the
     * label span because the nested <sdc-checklist> of a parent entry carries the same id.
     */
    type: (label: string) => `span[data-tests-id="checklist-type-${label}"]`,
    status: (label: string) => `span[data-tests-id="checklist-status-${label}"]`,
    category: (...path: string[]) =>
        `span[data-tests-id="checklist-category-${path.map(p => p.toLowerCase().replace(/ /g, '-')).join('-')}"]`,
};

/** The label `data-tests-id` of each catalog type checkbox, by the type code its tiles carry. */
export const TYPE_FILTER: Record<string, string> = {
    VF: 'resource-virtual-function-(vf)',
    VFC: 'resource-virtual-function-component-(vfc)',
    CR: 'resource-custom-resource-(cr)',
    PNF: 'resource-physical-network-function-(pnf)',
    CP: 'resource-connection-point-(cp)',
    VL: 'resource-virtual-link-(vl)',
    S: 'service',
};

/**
 * Loads the catalog from cold (so it fetches the current item list) and waits for the first tile.
 * The page renders nothing until the whole catalog has loaded, which on a long-lived stack with
 * thousands of items took over a minute under parallel load; hence the long wait.
 */
export async function openCatalog(page: Page, path = '/catalog'): Promise<void> {
    await gotoCold(page, path);
    await expect(page.locator(CAT.tile).first(), 'the catalog rendered no tile').toBeVisible({ timeout: 180_000 });
}

/** The tile of the asset named `name`. Matches the icon's exact `data-tests-id`, not a substring. */
export function tile(page: Page, name: string) {
    return page.locator(CAT.tile).filter({ has: page.locator(`[data-tests-id="${name}"]`) });
}

export async function searchCatalog(page: Page, term: string): Promise<void> {
    await page.locator(CAT.search).fill(term);
}

/** Ticks or unticks a checklist entry by clicking its label, which toggles the input it wraps. */
export async function setFilter(page: Page, selector: string, checked: boolean): Promise<void> {
    const box = page.locator(selector).locator('xpath=..').locator('input');
    if ((await box.isChecked()) !== checked) {
        await page.locator(selector).click();
    }
    await expect(box).toBeChecked({ checked });
}

/** The asset names of the rendered tiles, in display order. */
export async function tileNames(page: Page): Promise<string[]> {
    return page.locator(`${CAT.tile} .sdc-tile-content-icon span[data-tests-id]`)
        .evaluateAll(els => els.map(e => e.getAttribute('data-tests-id') || ''));
}

async function expectStatus(resp: APIResponse, status: number, what: string): Promise<void> {
    if (resp.status() !== status) {
        expect(resp.status(), `${what} failed: ${(await resp.text()).slice(0, 1000)}`).toBe(status);
    }
}

export async function lifecycle(api: SdcApi, asset: CreatedAsset, action: 'checkin' | 'checkout' | 'certify'): Promise<CreatedAsset> {
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/lifecycleState/${action}`),
        { headers: api.headers, data: { userRemarks: `${action} by the Playwright suite` } });
    await expectStatus(resp, 200, `${action} of ${asset.name}`);
    return { ...asset, id: (await resp.json()).uniqueId };
}

export async function metadata(api: SdcApi, asset: CreatedAsset): Promise<any> {
    const resp = await api.request.get(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/filteredDataByParams?include=metadata`),
        { headers: api.headers });
    await expectStatus(resp, 200, `reading ${asset.name}`);
    return (await resp.json()).metadata;
}

/** A VF / PNF / CR in a given category, created through `POST /catalog/resources`. */
export async function createResource(
    api: SdcApi, resourceType: 'VF' | 'PNF' | 'CR', namePrefix: string,
    category = { name: 'Generic', uniqueId: 'resourceNewCategory.generic', sub: 'Abstract', subId: 'resourceNewCategory.generic.abstract' },
): Promise<CreatedAsset> {
    const name = uniqueName(namePrefix);
    const resp = await api.postCreate('/catalog/resources', {
        data: {
            name,
            description: 'created by the Playwright suite',
            componentType: 'RESOURCE',
            resourceType,
            categories: [{
                name: category.name, uniqueId: category.uniqueId,
                subcategories: [{ name: category.sub, uniqueId: category.subId }],
            }],
            vendorName: 'pwVendor',
            vendorRelease: '1.0',
            contactId: DESIGNER_USER,
            icon: 'defaulticon',
            tags: [name],
        },
    });
    return api.readable(resp, name, 'resource');
}

// ---------------------------------------------------------------------------
// Archive / restore
// ---------------------------------------------------------------------------

export async function archive(api: SdcApi, asset: CreatedAsset, action: 'archive' | 'restore'): Promise<void> {
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/${action}`), { headers: api.headers, data: {} });
    await expectStatus(resp, 200, `${action} of ${asset.name}`);
}

/** Instantiates `child` (by its current id and version) in the service. */
export async function addInstance(api: SdcApi, service: CreatedAsset, child: CreatedAsset): Promise<void> {
    const meta = await metadata(api, child);
    const resp = await api.request.post(api.url(`/catalog/services/${service.id}/resourceInstance`), {
        headers: api.headers,
        data: {
            componentUid: child.id, componentVersion: meta.version, name: child.name,
            originType: meta.resourceType, icon: meta.icon, posX: '300', posY: '200',
        },
    });
    await expectStatus(resp, 201, `adding ${child.name} to ${service.name}`);
}

/** The archive and restore actions of the onboarding side, which mark the VSP's VFs `vspArchived`. */
export async function vspAction(api: SdcApi, vsp: OnboardingItem, action: 'ARCHIVE' | 'RESTORE'): Promise<void> {
    const resp = await api.request.put(api.onboardingUrl(`/items/${vsp.id}/actions`), { headers: api.headers, data: { action } });
    await expectStatus(resp, 200, `${action} of VSP ${vsp.name}`);
}

/**
 * A submitted and packaged VSP built from `packageFile` (a path under Files/), and the VF created from
 * it, as "Import VSP" + Create does. The VF is checked out at 0.1.
 */
export async function onboardedVf(api: SdcApi, packageFile = 'VNFs/base_vvg.zip', namePrefix = 'PwArchVsp')
    : Promise<{ vlm: OnboardingItem; vsp: OnboardingItem; vf: CreatedAsset }> {
    const vlm = await api.createVlm();
    await api.submitVlm(vlm);
    const vsp = await api.createVsp(vlm, namePrefix);
    const base = `/vendor-software-products/${vsp.id}/versions/${vsp.versionId}`;
    const file = fixtureFile(packageFile);

    const upload = await api.request.post(api.onboardingUrl(`${base}/orchestration-template-candidate`), {
        headers: { USER_ID: DESIGNER_USER },
        multipart: { upload: { name: path.basename(file), mimeType: 'application/octet-stream', buffer: fs.readFileSync(file) } },
    });
    await expectStatus(upload, 200, `uploading ${packageFile} to ${vsp.name}`);
    const processed = await api.request.put(api.onboardingUrl(`${base}/orchestration-template-candidate/process`),
        { headers: api.headers, data: {} });
    await expectStatus(processed, 200, `processing the package of ${vsp.name}`);
    expect((await processed.json()).status, `processing ${packageFile} reported errors`).toBe('Success');
    for (const data of [{ action: 'Submit', submitRequest: { message: 'Submit' } }, { action: 'Create_Package' }]) {
        const resp = await api.request.put(api.onboardingUrl(`${base}/actions`), { headers: api.headers, data });
        await expectStatus(resp, 200, `${data.action} of ${vsp.name}`);
    }

    const resp = await api.postCreate('/catalog/resources', {
        timeout: 120_000,
        data: {
            name: vsp.name,
            description: 'created by the Playwright suite',
            componentType: 'RESOURCE',
            resourceType: 'VF',
            categories: [{
                name: 'Generic', uniqueId: 'resourceNewCategory.generic',
                subcategories: [{ name: 'Abstract', uniqueId: 'resourceNewCategory.generic.abstract' }],
            }],
            vendorName: vlm.name,
            vendorRelease: '1.0',
            resourceVendorModelNumber: '',
            contactId: DESIGNER_USER,
            icon: 'defaulticon',
            tags: [vsp.name],
            csarUUID: vsp.id,
            csarVersion: '1.0',
            csarVersionId: vsp.versionId,
        },
    });
    return { vlm, vsp, vf: await api.readable(resp, vsp.name, 'resource') };
}
