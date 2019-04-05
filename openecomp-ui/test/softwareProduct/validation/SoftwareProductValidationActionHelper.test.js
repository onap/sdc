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
import { VSPTestsRequestFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPGeneralInfoFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPTestResultsSuccessFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationResultsFactory.js';
import { mapActionsToProps } from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidation.js';

describe('Software Product Validation Action Helper Tests', function() {
    const store = storeCreator();
    deepFreeze(store.getState());

    const vspChecksList = VSPChecksFactory.build();
    const vspTestsMap = VSPTestsMapFactory.build();
    const certificationChecked = VSPCertificationCheckedFactory.build();
    const complianceChecked = VSPComplianceCheckedFactory.build();
    const activeTab = { activeTab: tabsMapping.INPUTS };
    const errorMessage = { msg: 'Test Error Message' };
    const testsRequest = VSPTestsRequestFactory.build();
    const generalInfo = VSPGeneralInfoFactory.build();
    const isValidationDisabled = false;
    const vspTestResults = VSPTestResultsSuccessFactory.build();
    let restPrefix = Configuration.get('restPrefix');
    let onClose = () => {};

    const modal = {
        type: 'error',
        title: 'Error',
        modalComponentName: 'Error',
        modalComponentProps: {
            onClose: onClose
        },
        msg: {
            msg: 'Test Error Message'
        },
        cancelButtonText: 'OK'
    };
    // deepFreeze(vspTestsMap.vspTestsMap);
    // deepFreeze(certificationChecked.certificationChecked);
    // deepFreeze(complianceChecked.complianceChecked);
    // deepFreeze(activeTab);
    // deepFreeze(testsRequest);
    // deepFreeze(generalInfo.generalInfo);
    // deepFreeze(isVspValidationDisabled);

    it('Software Products Validation Action Helper : fetch vsp', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.vspChecks',
            vspChecksList
        );
        mockRest.addHandler('fetch', ({ baseUrl }) => {
            expect(baseUrl).toEqual(
                `${restPrefix}/v1.0/externaltesting/testcasetree`
            );
            return vspChecksList;
        });
        return SoftwareProductValidationActionHelper.fetchVspChecks(
            store.dispatch
        )
            .then(() => {
                expect(store.getState()).toEqual(expectedStore);
            })
            .catch(() => {
                console.log('Fetch VSP returned Error');
            });
    });

    // it('Software Products Validation Action Helper : post test', () => {
    //     mockRest.addHandler('post', ({ options, data, baseUrl }) => {
    //         expect(baseUrl).toEqual(
    //             `${restPrefix}/v1.0/externaltesting/executions`
    //         );
    //         //expect(data).toEqual(testsRequest);
    //         expect(options).toEqual(undefined);
    //         return { vspTestResults: vspTestResults };
    //     });
    //     const version = {
    //         id: 12345,
    //         name: 1
    //     };
    //     const softwareProductId = '1234';
    //     const status = 'draft';
    //     mapActionsToProps(store.dispatch).onTestSubmit(
    //         softwareProductId,
    //         version,
    //         status,
    //         testsRequest
    //     );
    // });

    it('Software Products Validation Action Helper : setCertificationChecked', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.certificationChecked',
            certificationChecked.certificationChecked
        );
        mapActionsToProps(store.dispatch).setCertificationChecked({
            checked: certificationChecked.certificationChecked
        });
        expect(store.getState()).toEqual(expectedStore);
    });

    it('Software Products Validation Action Helper : onErrorThrown', () => {
        let expectedStore = cloneAndSet(store.getState(), 'modal', modal);
        mapActionsToProps(store.dispatch).onErrorThrown(errorMessage);
        expect(JSON.stringify(store.getState())).toEqual(
            JSON.stringify(expectedStore)
        );
    });

    it('Software Products Validation Action Helper : setComplianceChecked', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.complianceChecked',
            complianceChecked.complianceChecked
        );
        mapActionsToProps(store.dispatch).setComplianceChecked({
            checked: complianceChecked.complianceChecked
        });
        expect(store.getState()).toEqual(expectedStore);
    });

    it('Software Products Validation Action Helper : setActiveTab', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.activeTab',
            activeTab.activeTab
        );
        mapActionsToProps(store.dispatch).setActiveTab(activeTab);
        expect(store.getState()).toEqual(expectedStore);
    });

    it('Software Products Validation Action Helper : setGeneralInfo', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.generalInfo',
            generalInfo.generalInfo
        );
        mapActionsToProps(store.dispatch).setGeneralInfo(
            generalInfo.generalInfo
        );
        expect(store.getState()).toEqual(expectedStore);
    });

    it('Software Products Validation Action Helper : setIsVspValidationDisabled', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.isValidationDisabled',
            isValidationDisabled
        );
        SoftwareProductValidationActionHelper.setIsVspValidationDisabled(
            store.dispatch,
            {
                isValidationDisabled
            }
        );
        expect(store.getState()).toEqual(expectedStore);
    });

    it('Software Products Validation Action Helper : setTestsRequest', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.testsRequest',
            testsRequest.testsRequest
        );
        expectedStore = cloneAndSet(
            expectedStore,
            'softwareProduct.softwareProductValidation.generalInfo',
            generalInfo.generalInfo
        );

        mapActionsToProps(store.dispatch).setTestsRequest(
            testsRequest.testsRequest,
            generalInfo.generalInfo
        );
        expect(store.getState()).toEqual(expectedStore);
    });

    it('Software Products Validation Action Helper : setVspTestsMap', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidation.vspTestsMap',
            vspTestsMap.vspTestsMap
        );
        SoftwareProductValidationActionHelper.setVspTestsMap(
            store.dispatch,
            vspTestsMap.vspTestsMap
        );

        expect(store.getState()).toEqual(expectedStore);
    });
});
