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
import SoftwareProductValidationResultsViewActionHelper from 'sdc-app/onboarding/softwareProduct/validationResults/SoftwareProductValidationResultsViewActionHelper.js';
import { tabsMapping } from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidationConstants.js';

import Configuration from 'sdc-app/config/Configuration.js';


 import { VSPTestResultsSuccessFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationResultsFactory.js';
import { VSPTestResultsFailureFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationResultsFactory.js';
import { VSPTestsMapFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPChecksFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPTestsRequestFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPGeneralInfoFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import VersionFactory from 'test-utils/factories/common/VersionFactory.js';


describe('Software Product Validation Test Result Action Helper Tests', function() {
    const store = storeCreator();
    deepFreeze(store.getState());
    const version = VersionFactory.build();
    const vspId = 10000;
    const vspChecksList = VSPChecksFactory.build();
    const vspTestsMap = VSPTestsMapFactory.build();

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

    it('Software Products Validation Test Result Action Helper : fetch vsp', () => {
        let expectedStore = cloneAndSet(
            store.getState(),
            'softwareProduct.softwareProductValidationResult.vspChecks',
            vspChecksList
        );
        mockRest.addHandler('fetch', ({ baseUrl }) => {
            expect(baseUrl).toEqual(
                `${restPrefix}/v1.0/externaltesting/testcasetree`
            );
            return vspChecksList;
        });
        return SoftwareProductValidationResultsViewActionHelper.fetchVspChecks(
            store.dispatch
        )
            .then(() => {
            var stat  = store.getState();
               expect(stat).toEqual(expectedStore);
            })
            .catch((e) => {
                console.log('Fetch VSP returned Error');
            });
    });
     it('Software Products Validation Test Result Action Helper : RefreshValidationResults', () => {
            let expectedStore = cloneAndSet(
                store.getState(),
                'softwareProduct.softwareProductValidationResult.vspTestResults',
                vspTestResults.vspTestResults
            );

            mockRest.addHandler('fetch', ({ baseUrl }) => {
                expect(baseUrl).toEqual(
                  `${restPrefix}/v1.0/externaltesting/vspid/${vspId}/vspversion/${version.id}`
                );
                return vspTestResults.vspTestResults;
            });
            return SoftwareProductValidationResultsViewActionHelper.refreshValidationResults(
                store.dispatch, {
                      vspId: vspId,
                      versionId: version.id
                  }
            )
                .then((e) => {
                    var stt = store.getState();
                    expect(stt).toEqual(expectedStore);
                })
                .catch((e) => {
                    console.log('Fetch VSP Test Result returned Error',e);
                 });
     });
})
