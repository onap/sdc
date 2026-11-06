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
 * ETSI SOL004 VNF/CNF onboarding, and creating a VLM through the onboarding UI. The onboarded ETSI
 * package must travel unchanged into the VF's CSAR and, nested under the VF, into the service's CSAR.
 * CREATES DATA (a VLM, a VSP, a VF and a service per test).
 */
import * as path from 'path';
import { test, expect, loaderGone, uniqueName, OnboardingItem } from './fixtures/sdc';
import { Csar, fixtureFile } from './fixtures/csar';
import {
    OB, addInstance, certify, clickSubmit, componentData, downloadToscaCsarViaUi, importVspViaUi, itemVersion, onboardViaUi,
    openNewVsp, openOnboardHome, openVspInRepository, vlmByName,
} from './fixtures/onboarding';

test.describe.configure({ timeout: 180_000 });

function entriesUnder(csar: Csar, folder: string): string[] {
    return csar.entries().filter(e => e.startsWith(folder));
}

test('create and submit a VLM', async ({ sdcPage, api }) => {
    const vendorName = uniqueName('PwObVlm');
    await openOnboardHome(sdcPage);
    await sdcPage.locator(OB.addNewVlm).click();
    await sdcPage.locator('[data-test-id="vendor-name"]').fill(vendorName);
    await sdcPage.locator('[data-test-id="vendor-description"]').fill('My New VLM');
    const created = sdcPage.waitForResponse(r => r.request().method() === 'POST' && /\/vendor-license-models\/?$/.test(r.url()));
    await sdcPage.locator(OB.formSubmit).click();
    const resp = await created;
    expect(resp.status()).toBe(200);
    const body = await resp.json();
    const vlm: OnboardingItem = { id: body.itemId, versionId: body.version.id, name: vendorName };

    await expect(sdcPage.locator('[data-test-id="navbar-group-name"]')).toHaveText(vendorName, { timeout: 30_000 });
    await expect(sdcPage.locator(`${OB.nav('LICENSE_MODEL_OVERVIEW')}.selected`)).toBeVisible();
    await loaderGone(sdcPage);
    const submitted = sdcPage.waitForResponse(r => r.request().method() === 'PUT'
        && r.url().endsWith(`/vendor-license-models/${vlm.id}/versions/${vlm.versionId}/actions`));
    await clickSubmit(sdcPage, 'vendor-license-models', vlm);
    expect((await submitted).status()).toBe(200);

    expect(await vlmByName(api, vendorName), `${vendorName} is not listed`).toMatchObject({ id: vlm.id, description: 'My New VLM' });
    await expect.poll(async () => (await itemVersion(api, vlm)).status).toBe('Certified');
});

for (const pkg of ['ETSI/ETSI-VNF-SAMPLE.csar', 'ETSI/ETSI-CNF-SAMPLE.csar']) {
    test(`${pkg}: the ETSI package reaches the VF and the service CSAR`, async ({ sdcPage, api }) => {
        const packageName = path.basename(pkg);
        const { vsp } = await openNewVsp(sdcPage, api, 'PwObEtsi');
        await onboardViaUi(sdcPage, api, vsp, fixtureFile(pkg));
        await openVspInRepository(sdcPage, vsp.name);
        const vf = await importVspViaUi(sdcPage, api, vsp.name);

        const artifacts = Object.values((await componentData(api, vf, 'deploymentArtifacts')).deploymentArtifacts || {});
        expect(artifacts.map((a: any) => [a.artifactType, a.artifactName])).toContainEqual(['ETSI_PACKAGE', packageName]);

        const certifiedVf = await certify(api, vf);
        expect(certifiedVf.lifecycleState).toBe('CERTIFIED');
        const vfCsar = await downloadToscaCsarViaUi(sdcPage, certifiedVf);
        expect(entriesUnder(vfCsar, 'Artifacts/Deployment/ETSI_PACKAGE/'), `VF CSAR entries: ${vfCsar.entries().join(', ')}`)
            .toEqual([`Artifacts/Deployment/ETSI_PACKAGE/${packageName}`]);

        const service = await api.createService('PwObEtsiSvc');
        await addInstance(api, service, (await componentData(api, certifiedVf, 'metadata')).metadata);
        const certifiedService = await certify(api, service);
        expect(certifiedService.lifecycleState).toBe('CERTIFIED');
        const nodeType = `org.openecomp.resource.vf.${vsp.name.charAt(0).toUpperCase()}${vsp.name.slice(1).toLowerCase()}`;
        const folder = `Artifacts/${nodeType}_v1.0/Deployment/ETSI_PACKAGE/`;
        const serviceCsar = await downloadToscaCsarViaUi(sdcPage, certifiedService);
        expect(entriesUnder(serviceCsar, folder), `service CSAR entries: ${serviceCsar.entries().join(', ')}`)
            .toEqual([`${folder}${packageName}`]);
    });
}
