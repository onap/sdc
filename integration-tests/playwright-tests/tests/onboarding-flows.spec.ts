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
 * Package onboarding: a VSP shell is seeded through the API, and the package is uploaded, validated
 * and submitted through the onboarding UI, then imported as a VF from catalog-ui's VSP repository.
 * CREATES DATA (a VLM, a VSP and usually a VF and a service per test).
 */
import { test, expect, loaderGone, SEL } from './fixtures/sdc';
import { fixtureFile } from './fixtures/csar';
import {
    OB, addInstance, certify, componentData, downloadVspCsarViaUi, expectedValidationFragment, importVspViaUi,
    itemVersion, onboardViaUi, openNewVsp, openVspDraft, openVspInRepository, processPackage, submittedVlm,
    uploadPackage, vspPackages,
} from './fixtures/onboarding';

test.describe.configure({ timeout: 180_000 });

/** Every `{fileName, type}` in HEAT.meta's import structure, nested env/volume/heat entries included. */
function heatMetaArtifacts(node: any, out: { fileName: string; type: string }[] = []): { fileName: string; type: string }[] {
    if (Array.isArray(node)) {
        node.forEach(n => heatMetaArtifacts(n, out));
    } else if (node && typeof node === 'object') {
        if (typeof node.fileName === 'string' && typeof node.type === 'string') {
            out.push({ fileName: node.fileName, type: node.type });
        }
        Object.values(node).forEach(v => heatMetaArtifacts(v, out));
    }
    return out;
}

for (const pkg of ['VNFs/base_vvg.zip', 'CNFs/helm-package-valid.zip']) {
    test(`${pkg}: onboard, import as a VF and certify it inside a service`, async ({ sdcPage, api }) => {
        const file = fixtureFile(pkg);
        const { vlm, vsp } = await openNewVsp(sdcPage, api, 'PwObVsp');

        const validation = await onboardViaUi(sdcPage, api, vsp, file);
        expect(JSON.parse(validation!).status, validation).toBe('Success');
        const fragment = expectedValidationFragment(file);
        if (fragment) {
            expect(validation).toContain(fragment);
        }

        await openVspInRepository(sdcPage, vsp.name);
        const vspCsar = await downloadVspCsarViaUi(sdcPage);
        const expected = heatMetaArtifacts(JSON.parse(await vspCsar.text('Artifacts/HEAT.meta')));
        expect(expected.length, 'HEAT.meta lists no artifacts').toBeGreaterThan(0);

        const vf = await importVspViaUi(sdcPage, api, vsp.name);
        const created = await componentData(api, vf, 'metadata', 'deploymentArtifacts');
        expect(created.metadata).toMatchObject({
            name: vsp.name, resourceType: 'VF', csarUUID: vsp.id, csarVersion: '1.0', vendorName: vlm.name,
        });
        const deployed = Object.values(created.deploymentArtifacts || {})
            .map((a: any) => ({ fileName: a.artifactName, type: a.artifactType, version: a.artifactVersion }));
        for (const a of expected) {
            expect(deployed, `VF deployment artifacts lack ${a.fileName}`).toContainEqual({ ...a, version: '1' });
        }

        const certifiedVf = await certify(api, vf);
        expect([certifiedVf.lifecycleState, certifiedVf.version]).toEqual(['CERTIFIED', '1.0']);
        const service = await api.createService('PwObSvc');
        const vfData = (await componentData(api, certifiedVf, 'metadata')).metadata;
        await addInstance(api, service, vfData);
        const certifiedService = await certify(api, service);
        expect(certifiedService.lifecycleState).toBe('CERTIFIED');
        const instances = (await componentData(api, certifiedService, 'componentInstances')).componentInstances;
        expect(instances.map((i: any) => i.componentUid)).toEqual([certifiedVf.id]);
    });
}

test('a Helm package without an isBase chart is rejected on upload', async ({ sdcPage, api }) => {
    const { vsp } = await openNewVsp(sdcPage, api, 'PwObBadHelm');
    const upload = sdcPage.waitForResponse(r => r.request().method() === 'POST'
        && r.url().endsWith(`/vendor-software-products/${vsp.id}/versions/${vsp.versionId}/orchestration-template-candidate`));
    await sdcPage.locator(OB.packageInput).setInputFiles(fixtureFile('CNFs/invalid/helm-package-invalid-missing-flag-isbase.zip'));
    const resp = await upload;
    expect(resp.status()).toBe(406);
    const messages = ((await resp.json()).errors.uploadFile || []).map((e: any) => e.message);
    expect(messages).toContain("None of charts is marked as 'isBase'.");

    const dialog = sdcPage.getByRole('dialog').filter({ hasText: 'Upload validation failed' });
    await expect(dialog).toBeVisible();
    await expect(dialog).toContainText("None of charts is marked as 'isBase'.");

    expect((await itemVersion(api, vsp)).status).toBe('Draft');
    expect((await vspPackages(api)).map(p => p.packageId)).not.toContain(vsp.id);
});

test('validation pages of a VSP with a processed package', async ({ sdcPage, api }) => {
    const vsp = await api.createVsp(await submittedVlm(api), 'PwObValid');
    await uploadPackage(api, vsp, fixtureFile('VNFs/base_vvg.zip'));
    expect((await processPackage(api, vsp)).status).toBe('Success');
    const config = await api.request.get(api.onboardingUrl('/externaltesting/config'), { headers: api.headers });
    expect(config.status()).toBe(200);
    const enabled = (await config.json()).enabled === true;

    await openVspDraft(sdcPage, vsp);
    const validationLink = sdcPage.locator(OB.nav('SOFTWARE_PRODUCT_VALIDATION'));
    const resultsLink = sdcPage.locator(OB.nav('SOFTWARE_PRODUCT_VALIDATION_RESULTS'));
    if (!enabled) {
        await expect(validationLink).toBeHidden();
        await expect(resultsLink).toBeHidden();
        return;
    }
    await expect(validationLink).toBeVisible();
    await expect(resultsLink).toBeVisible();

    await validationLink.click();
    await expect(sdcPage.locator('[data-test-id="go-to-vsp-validation-inputs"]')).toBeDisabled({ timeout: 30_000 });
    await resultsLink.click();
    await loaderGone(sdcPage);
    await expect(sdcPage.getByText('No Test Performed')).toBeVisible({ timeout: 30_000 });
});

test('PNF package: the imported PNF carries the software versions of its package', async ({ sdcPage, api }) => {
    const softwareVersions = ['5gDUv18.05.201', '5gDUv18.06.205'];
    const { vsp } = await openNewVsp(sdcPage, api, 'PwObPnf');
    await onboardViaUi(sdcPage, api, vsp, fixtureFile('PNFs/sample-pnf-1.0.1-SNAPSHOT.csar'));
    await openVspInRepository(sdcPage, vsp.name);
    const pnf = await importVspViaUi(sdcPage, api, vsp.name);

    const stored = await componentData(api, pnf, 'metadata', 'properties');
    expect(stored.metadata.resourceType).toBe('PNF');
    const property = (stored.properties || []).find((p: any) => p.name === 'software_versions');
    expect(property, `${pnf.name} has no software_versions property`).toBeTruthy();
    expect(JSON.parse(property.value).sort()).toEqual([...softwareVersions].sort());

    await sdcPage.locator(SEL.sideMenu('Properties Assignment')).click();
    const values = sdcPage.locator('input[data-tests-id^="value-prop-software_versions"]');
    await expect(values).toHaveCount(softwareVersions.length, { timeout: 30_000 });
    expect((await values.evaluateAll(els => els.map(e => (e as HTMLInputElement).value))).sort())
        .toEqual([...softwareVersions].sort());
});
