/*
 * Copyright © 2019 Vodafone Group
 *
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
 */
import deepFreeze from 'deep-freeze';
import mockRest from 'test-utils/MockRest.js';
import { cloneAndSet } from 'test-utils/Util.js';
import { storeCreator } from 'sdc-app/AppStore.js';
import SoftwareProductValidationActionHelper from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidationActionHelper.js';
import { tabsMapping } from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidationConstants.js';

import Configuration from 'sdc-app/config/Configuration.js';

import { VSPComplianceCheckedFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPCertificationCheckedFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPChecksFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPTestsMapFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';

describe('Software Product Validation Action Helper Tests', function() {
    it('Software Products Validation Action Helper : Dsspatch', () => {
        const store = storeCreator();
        deepFreeze(store.getState());

        const vspChecksList = VSPChecksFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const certificationChecked = VSPCertificationCheckedFactory.build();
        const complianceChecked = VSPComplianceCheckedFactory.build();
        const activeTab = { activeTab: tabsMapping.INPUTS };
        const errorMessage = { msg: 'Test Error Message' };

        deepFreeze(vspChecksList);
        deepFreeze(vspTestsMap);
        deepFreeze(certificationChecked);
        deepFreeze(complianceChecked);
        deepFreeze(activeTab);

        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.vspChecks',
            vspChecksList
        );
        expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.vspTestsMap',
            vspTestsMap
        );
        expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.certificationChecked',
            certificationChecked
        );
        expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.complianceChecked',
            complianceChecked
        );
        expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.activeTab',
            activeTab
        );
        let restPrefix = Configuration.get('restPrefix');

        mockRest.addHandler('fetch', ({ options, data, baseUrl }) => {
            expect(baseUrl).toEqual(`${restPrefix}/v1.0/externaltesting`);
            expect(data).toEqual(undefined);
            expect(options).toEqual(undefined);
            return { vspChecks: vspChecksList };
        });

        SoftwareProductValidationActionHelper.setVspTestsMap(store.dispatch, {
            vspTestsMap
        });
        SoftwareProductValidationActionHelper.setComplianceChecked(
            store.dispatch,
            { complianceChecked }
        );
        SoftwareProductValidationActionHelper.setCertificationChecked(
            store.dispatch,
            { certificationChecked }
        );

        SoftwareProductValidationActionHelper.setActiveTab(store.dispatch, {
            activeTab
        });

        SoftwareProductValidationActionHelper.onErrorThrown(store.dispatch, {
            errorMessage
        });
        // let tests = [
        //     {
        //         testId: 'certquery',
        //         parameterValues: [
        //             {
        //                 id: 'vspId',
        //                 value: '111'
        //             },
        //             {
        //                 id: 'vspVersion',
        //                 value: '1.0'
        //             }
        //         ]
        //     }
        // ];

        SoftwareProductValidationActionHelper.fetchVspChecks(store.dispatch)
            .then(() => {
                expect(store.getState()).toEqual(expectedStore);
            })
            .catch(() => {});
    });
});
