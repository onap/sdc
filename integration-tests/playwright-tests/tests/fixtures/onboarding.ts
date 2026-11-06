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
 * Onboarding (VLM/VSP) and VSP-import helpers: REST seeding plus the UI steps the onboarding specs
 * share. The onboarding UI is the openecomp-ui punch-out, which has no URL routing (the hash stays
 * #!/onboardVendor), so its pages are reached by clicking.
 */

import * as fs from 'fs';
import * as path from 'path';
import { APIResponse, Page } from '@playwright/test';
import { expect, gotoCold, gotoWorkspaceTab, loaderGone, SdcApi, OnboardingItem, CreatedAsset, SEL, uniqueName, DESIGNER_USER } from './sdc';
import { downloadCsar, Csar } from './csar';

export const OB = {
    filters: '[data-test-id="catalog-filter-items-type"]',
    workspaceTab: '[data-test-id="onboard-workspace-tab"]',
    onboardCatalogTab: '[data-test-id="onboard-onboard-tab"]',
    itemName: '[data-test-id="catalog-item-name"]',
    addNewVlm: '[data-test-id="catalog-add-new-vlm"]',
    nav: (screen: string) => `[data-test-id="navbar-group-item-${screen}"]`,
    packageInput: 'input[name="fileInput"]',
    proceedToValidation: '[data-test-id="proceed-to-validation-btn"]',
    attachmentsValidationTab: '[data-test-id="attachments-tab-validation"]',
    validationNode: '[data-test-id="validation-tree-node"]',
    validationNodeName: '[data-test-id="validation-tree-node-name"]',
    errorCount: '[data-test-id="validation-error-count"]',
    warningCount: '[data-test-id="validation-warning-count"]',
    submit: '[data-test-id="vc-submit-btn"]',
    commitComment: '[data-test-id="commit-comment-text"]',
    formSubmit: '[data-test-id="form-submit-button"]',
};

const onboardingPath = (vsp: OnboardingItem) => `/vendor-software-products/${vsp.id}/versions/${vsp.versionId}`;

async function expectStatus(resp: APIResponse, status: number, what: string): Promise<void> {
    if (resp.status() !== status) {
        expect(resp.status(), `${what} failed: ${(await resp.text()).slice(0, 1000)}`).toBe(status);
    }
}

// ---------------------------------------------------------------------------
// Onboarding REST
// ---------------------------------------------------------------------------

export async function submittedVlm(api: SdcApi, prefix = 'PwObVlm'): Promise<OnboardingItem> {
    const vlm = await api.createVlm(prefix);
    await api.submitVlm(vlm);
    return vlm;
}

/** What the UI's upload does: stores the package as the VSP's orchestration-template candidate. */
export async function uploadPackage(api: SdcApi, vsp: OnboardingItem, file: string): Promise<void> {
    const resp = await api.request.post(api.onboardingUrl(`${onboardingPath(vsp)}/orchestration-template-candidate`), {
        headers: { USER_ID: DESIGNER_USER },
        multipart: {
            upload: { name: path.basename(file), mimeType: 'application/octet-stream', buffer: fs.readFileSync(file) },
        },
    });
    await expectStatus(resp, 200, `uploading ${path.basename(file)} to ${vsp.name}`);
}

/** What "Proceed to Validation" does. Returns the validation response (`status`, `errors`, …). */
export async function processPackage(api: SdcApi, vsp: OnboardingItem): Promise<any> {
    const resp = await api.request.put(
        api.onboardingUrl(`${onboardingPath(vsp)}/orchestration-template-candidate/process`), { headers: api.headers });
    await expectStatus(resp, 200, `processing the package of ${vsp.name}`);
    return resp.json();
}

/** `GET /items/{id}/versions/{versionId}`: `status` is Draft or Certified. */
export async function itemVersion(api: SdcApi, item: OnboardingItem): Promise<any> {
    const resp = await api.request.get(
        api.onboardingUrl(`/items/${item.id}/versions/${item.versionId}`), { headers: api.headers });
    await expectStatus(resp, 200, `reading ${item.name}`);
    return resp.json();
}

/** The VSPs catalog-ui's VSP repository lists: submitted VSPs with a created package. */
export async function vspPackages(api: SdcApi): Promise<any[]> {
    const resp = await api.request.get(api.onboardingUrl('/vendor-software-products/packages'), { headers: api.headers });
    await expectStatus(resp, 200, 'listing VSP packages');
    return (await resp.json()).results || [];
}

export async function vlmByName(api: SdcApi, name: string): Promise<any | undefined> {
    const resp = await api.request.get(api.onboardingUrl('/vendor-license-models'), { headers: api.headers });
    await expectStatus(resp, 200, 'listing VLMs');
    return ((await resp.json()).results || []).find((v: any) => v.name === name);
}

/** The `_expect` file next to a test package holds a fragment its validation response must contain. */
export function expectedValidationFragment(packageFile: string): string | undefined {
    const expectFile = packageFile.replace(/\.[^.\/]+$/, '') + '_expect';
    return fs.existsSync(expectFile) ? fs.readFileSync(expectFile, 'utf8').trim().replace(/[\n\r]/g, '') : undefined;
}

// ---------------------------------------------------------------------------
// Catalog REST
// ---------------------------------------------------------------------------

export async function resourceByName(api: SdcApi, name: string, version: string): Promise<any> {
    const resp = await api.request.get(
        api.url(`/catalog/resources/resourceName/${encodeURIComponent(name)}/resourceVersion/${version}`),
        { headers: api.headers });
    await expectStatus(resp, 200, `reading resource ${name} ${version}`);
    return resp.json();
}

export async function componentData(api: SdcApi, asset: CreatedAsset, ...include: string[]): Promise<any> {
    const query = include.map(i => `include=${i}`).join('&');
    const resp = await api.request.get(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/filteredDataByParams?${query}`),
        { headers: api.headers });
    await expectStatus(resp, 200, `reading ${include.join(', ')} of ${asset.name}`);
    return resp.json();
}

/** Certifies a checked-out component. Returns it under the id of its certified 1.0 version. */
export async function certify(api: SdcApi, asset: CreatedAsset): Promise<CreatedAsset & { lifecycleState: string; version: string }> {
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(asset.type)}/${asset.id}/lifecycleState/certify`),
        { headers: api.headers, data: { userRemarks: 'certified by the Playwright suite' } });
    await expectStatus(resp, 200, `certifying ${asset.name}`);
    const body = await resp.json();
    return { id: body.uniqueId, name: asset.name, type: asset.type, lifecycleState: body.lifecycleState, version: body.version };
}

export async function addInstance(api: SdcApi, container: CreatedAsset, resource: any): Promise<any> {
    const resp = await api.request.post(
        api.url(`/catalog/${SdcApi.collectionOf(container.type)}/${container.id}/resourceInstance`), {
            headers: api.headers,
            data: {
                componentUid: resource.uniqueId, componentVersion: resource.version, name: resource.name,
                originType: resource.resourceType, icon: resource.icon, posX: '300', posY: '200',
            },
        });
    await expectStatus(resp, 201, `adding ${resource.name} to ${container.name}`);
    return resp.json();
}

/** A PNF in NOT_CERTIFIED_CHECKOUT. */
export async function createPnf(api: SdcApi, namePrefix = 'PwPnf'): Promise<CreatedAsset> {
    const name = uniqueName(namePrefix);
    const resp = await api.postCreate('/catalog/resources', {
        data: {
            name,
            description: 'created by the Playwright suite',
            componentType: 'RESOURCE',
            resourceType: 'PNF',
            categories: [{
                name: 'Generic', normalizedName: 'generic', uniqueId: 'resourceNewCategory.generic',
                subcategories: [{ name: 'Abstract', normalizedName: 'abstract', uniqueId: 'resourceNewCategory.generic.abstract' }],
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
// Onboarding UI
// ---------------------------------------------------------------------------

export async function openOnboardHome(page: Page): Promise<void> {
    await gotoCold(page, '/onboardVendor');
    await page.locator(OB.filters).waitFor({ timeout: 60_000 });
    await loaderGone(page);
}

/** WORKSPACE opens an item with a single draft straight on that draft, skipping the versions page. */
export async function openVspDraft(page: Page, vsp: OnboardingItem): Promise<void> {
    await openOnboardHome(page);
    await page.locator(OB.workspaceTab).click();
    await page.locator(OB.itemName, { hasText: vsp.name }).first().click();
    await expect(page.locator('[data-test-id="navbar-group-name"]')).toHaveText(vsp.name, { timeout: 30_000 });
    await loaderGone(page);
}

/** Selects the package on the VSP landing page's file input, as the Browse button does. */
export async function uploadPackageViaUi(page: Page, vsp: OnboardingItem, file: string): Promise<void> {
    const upload = page.waitForResponse(r => r.request().method() === 'POST'
        && r.url().endsWith(`${onboardingPath(vsp)}/orchestration-template-candidate`), { timeout: 120_000 });
    await page.locator(OB.packageInput).setInputFiles(file);
    const resp = await upload;
    expect(resp.status(), `upload of ${path.basename(file)} failed: ${(await resp.text()).slice(0, 1000)}`).toBe(200);
    await expect(page.locator(`${OB.nav('SOFTWARE_PRODUCT_ATTACHMENTS')}.selected`)).toBeVisible({ timeout: 60_000 });
    await loaderGone(page);
}

/** Clicks "Proceed to Validation" and returns the backend's validation response, unparsed. */
export async function proceedToValidationViaUi(page: Page, vsp: OnboardingItem): Promise<string> {
    const processed = page.waitForResponse(r => r.request().method() === 'PUT'
        && r.url().endsWith(`${onboardingPath(vsp)}/orchestration-template-candidate/process`), { timeout: 120_000 });
    await page.locator(OB.proceedToValidation).click();
    const resp = await processed;
    expect(resp.status(), 'process request failed').toBe(200);
    await expect(page.locator(`${OB.attachmentsValidationTab}.sdc-tab-active`)).toBeVisible({ timeout: 60_000 });
    await loaderGone(page);
    return resp.text();
}

/**
 * Clicks the version controller's Submit for `item` (`collection` = vendor-license-models or
 * vendor-software-products). A dirty draft first raises the Commit & Submit modal; a clean one
 * submits straight away, so whichever comes first decides.
 */
export async function clickSubmit(page: Page, collection: string, item: OnboardingItem): Promise<void> {
    const submitted = page.waitForRequest(r => r.method() === 'PUT'
        && r.url().endsWith(`/${collection}/${item.id}/versions/${item.versionId}/actions`)
        && (r.postData() || '').includes('"Submit"'), { timeout: 120_000 });
    const comment = page.locator(OB.commitComment);
    await page.locator(OB.submit).click();
    const first = await Promise.race([
        submitted.then(() => 'submitted'),
        comment.waitFor({ state: 'visible', timeout: 120_000 }).then(() => 'modal'),
    ]);
    if (first === 'modal') {
        await comment.fill('submitted by the Playwright suite');
        await page.locator(OB.formSubmit).click();
        await submitted;
    }
}

/** Commit & Submit of a VSP draft. Returns once its package is created. */
export async function submitViaUi(page: Page, vsp: OnboardingItem): Promise<void> {
    const packaged = page.waitForResponse(r => r.request().method() === 'PUT'
        && r.url().endsWith(`${onboardingPath(vsp)}/actions`)
        && (r.request().postData() || '').includes('Create_Package'), { timeout: 120_000 });
    await clickSubmit(page, 'vendor-software-products', vsp);
    const resp = await packaged;
    expect(resp.status(), `creating the package of ${vsp.name} failed: ${await resp.text()}`).toBe(200);
    await loaderGone(page);
}

/** A submitted VLM, a VSP under it, and the VSP draft open in the onboarding UI. */
export async function openNewVsp(page: Page, api: SdcApi, prefix: string): Promise<{ vlm: OnboardingItem; vsp: OnboardingItem }> {
    const vlm = await submittedVlm(api);
    const vsp = await api.createVsp(vlm, prefix);
    await openVspDraft(page, vsp);
    return { vlm, vsp };
}

/**
 * Uploads, validates and submits `file` through the onboarding UI. Only a zip (HEAT/Helm) stops on
 * the attachments page for "Proceed to Validation"; its validation response is returned unparsed.
 */
export async function onboardViaUi(page: Page, api: SdcApi, vsp: OnboardingItem, file: string): Promise<string | undefined> {
    await uploadPackageViaUi(page, vsp, file);
    let validation: string | undefined;
    if (file.endsWith('.zip')) {
        validation = await proceedToValidationViaUi(page, vsp);
    }
    await submitViaUi(page, vsp);
    expect((await itemVersion(api, vsp)).status, `${vsp.name} is not certified after Submit`).toBe('Certified');
    return validation;
}

// ---------------------------------------------------------------------------
// VSP repository (catalog-ui)
// ---------------------------------------------------------------------------

export const REPO = {
    icon: '[data-tests-id="repository-icon"]',
    table: '[data-tests-id="importVspTable"]',
    search: '[data-tests-id="onboarding-search"] input',
    importButton: '[data-tests-id="import-csar"]',
    downloadCsar: '[data-tests-id="download-csar"]',
};

/** Opens the Vendor Software Product Repository from HOME and expands the row of `vspName`. */
export async function openVspInRepository(page: Page, vspName: string): Promise<void> {
    await gotoCold(page, '/dashboard');
    await page.locator(REPO.icon).click();
    const table = page.locator(REPO.table);
    await expect(table).toBeVisible({ timeout: 30_000 });
    await table.locator(REPO.search).fill(vspName);
    await table.locator('datatable-body-row', { hasText: vspName }).click();
    await expect(table.locator(REPO.importButton)).toBeVisible({ timeout: 30_000 });
}

export async function downloadVspCsarViaUi(page: Page): Promise<Csar> {
    return downloadCsar(page, () => page.locator(`${REPO.table} ${REPO.downloadCsar}`).click());
}

/**
 * Imports the expanded VSP and creates the VF from it on the General tab. Returns the created VF
 * (version 0.1, checked out).
 */
export async function importVspViaUi(page: Page, api: SdcApi, vspName: string): Promise<CreatedAsset> {
    await page.locator(`${REPO.table} ${REPO.importButton}`).click();
    await expect(page.locator(SEL.name)).toHaveValue(vspName, { timeout: 60_000 });
    await loaderGone(page);
    const created = page.waitForResponse(r => r.request().method() === 'POST'
        && /\/catalog\/resources$/.test(r.url()), { timeout: 180_000 });
    await page.locator(SEL.chromeCreateButton).click();
    const resp = await created;
    return api.readable(resp, vspName, 'resource');
}

/** Downloads the component's TOSCA CSAR from its TOSCA Artifacts tab. */
export async function downloadToscaCsarViaUi(page: Page, asset: CreatedAsset): Promise<Csar> {
    await gotoWorkspaceTab(page, { id: asset.id, type: asset.type, tab: 'tosca_artifacts', cold: true });
    await loaderGone(page);
    return downloadCsar(page, () => page.locator('[data-tests-id="download_Tosca Model"]').click());
}
