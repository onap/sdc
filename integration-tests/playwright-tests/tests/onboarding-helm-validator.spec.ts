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
 * Helm chart validation results on the VSP attachments page. NEEDS THE sdc-helm-validator
 * CONTAINER: without it the onboarding BE skips Helm validation and every chart looks clean.
 * Every test is tagged @helm-validator so a stack without it can grep the spec out.
 * CREATES DATA (a VLM and a VSP per test).
 */
import * as fs from 'fs';
import * as path from 'path';
import { test, expect } from './fixtures/sdc';
import { FILES_DIR } from './fixtures/csar';
import { OB, expectedValidationFragment, openNewVsp, proceedToValidationViaUi, uploadPackageViaUi } from './fixtures/onboarding';

type Outcome = 'clean' | 'warnings' | 'errors';

const DIRS: { dir: string; outcome: Outcome }[] = [
    { dir: 'CNFs/helm_validator', outcome: 'clean' },
    { dir: 'CNFs/helm_validator/with_warnings', outcome: 'warnings' },
    { dir: 'CNFs/helm_validator/invalid', outcome: 'errors' },
];

const CASES = DIRS.flatMap(({ dir, outcome }) => fs.readdirSync(path.join(FILES_DIR, dir))
    .filter(f => f.endsWith('.zip'))
    .sort()
    .map(f => ({ file: path.join(FILES_DIR, dir, f), outcome })));

for (const { file, outcome } of CASES) {
    test(`@helm-validator ${path.relative(FILES_DIR, file)}: charts validate with ${outcome === 'clean' ? 'no findings' : outcome}`,
        async ({ sdcPage, api }) => {
            const { vsp } = await openNewVsp(sdcPage, api, 'PwObHelm');
            await uploadPackageViaUi(sdcPage, vsp, file);
            const validation = await proceedToValidationViaUi(sdcPage, vsp);
            const fragment = expectedValidationFragment(file);
            if (fragment) {
                expect(validation).toContain(fragment);
            }

            const charts = sdcPage.locator(OB.validationNode)
                .filter({ has: sdcPage.locator(OB.validationNodeName, { hasText: /\.tgz$/ }) });
            await expect(charts.first()).toBeVisible();
            const withErrors = charts.filter({ has: sdcPage.locator(OB.errorCount) });
            const withWarnings = charts.filter({ has: sdcPage.locator(OB.warningCount) });
            const submit = sdcPage.locator(OB.submit);

            if (outcome === 'errors') {
                await expect(submit).toBeDisabled();
                await expect(withErrors).not.toHaveCount(0);
            } else {
                await expect(submit).toBeEnabled();
                await expect(withErrors).toHaveCount(0);
                if (outcome === 'warnings') {
                    await expect(withWarnings).not.toHaveCount(0);
                } else {
                    await expect(withWarnings).toHaveCount(0);
                }
            }
        });
}
