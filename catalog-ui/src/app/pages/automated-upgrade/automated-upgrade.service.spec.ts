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

// automated-upgrade.service.ts does `import Dictionary = _.Dictionary`, a runtime read of the global `_`.
(window as any)._ = require('lodash');
// tslint:disable-next-line:no-var-requires
const {AutomatedUpgradeService} = require('./automated-upgrade.service');
// tslint:disable-next-line:no-var-requires
const en = require('../../../assets/languages/en_US.json');

function serviceFor(subType: string) {
    const translateService: any = {translate: jest.fn((key: string) => key)};
    const service = new AutomatedUpgradeService(null, null, translateService);
    service['vspComponent'] = {name: 'vf1', version: '2.0', isResource: () => true, getComponentSubType: () => subType};
    service['componentType'] = 'RESOURCE';
    service['uiComponentsToUpgrade'] = [{isLock: false, isAlreadyUpgrade: false}];
    return {service, translateService};
}

describe('AutomatedUpgradeService texts', () => {
    it('names the resource type of the upgraded resource', () => {
        const {service, translateService} = serviceFor('CR');
        service.getInformationTextToDisplay();
        expect(translateService.translate).toHaveBeenCalledWith(
            'RESOURCE_AUTOMATED_UPGRADE_WITH_COMPONENTS_TO_UPGRADE', expect.objectContaining({resourceType: 'CR'}));
    });

    it('keeps the VF wording for a VF', () => {
        const fill = (s: string) => s.replace(/{{vspName}}/g, 'vf1').replace(/{{vspVersion}}/g, '2.0')
            .replace(/{{resourceType}}/g, 'VF').replace(/{{resourceName}}/g, 'vf1');
        expect(fill(en.RESOURCE_AUTOMATED_UPGRADE_WITH_COMPONENTS_TO_UPGRADE)).toContain('<b>"vf1"</b> VF.');
        expect(fill(en.RESOURCE_AUTOMATED_UPGRADE_ALL_COMPONENTS_LOCKED)).toContain('<b>"vf1"</b> VF, however');
        expect(fill(en.RESOURCE_AUTOMATED_UPGRADE_ALL_COMPONENTS_UPGRADED)).toContain('<b>"vf1"</b> VF and have');
        expect(fill(en.RESOURCE_CERTIFICATION_STATUS_TEXT)).toBe('The "vf1" VF was successfully updated and certified.');
        expect(fill(en.RESOURCE_NOTHING_TO_UPGRADE)).toContain('<b>"vf1"</b> VF.<br/>');
        expect(en.RESOURCE_NOTHING_TO_UPGRADE).toContain('{{resourceType}}');
        expect(en.RESOURCE_AUTOMATED_UPGRADE_WITH_COMPONENTS_TO_UPGRADE).toContain('{{resourceType}}');
    });
});
