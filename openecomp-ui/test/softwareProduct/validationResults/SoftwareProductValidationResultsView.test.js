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

import React from 'react';
import { mapStateToProps } from 'sdc-app/onboarding/softwareProduct/validationResults/SoftwareProductValidationResults.js';
import SoftwareProductValidationResultsView from 'sdc-app/onboarding/softwareProduct/validationResults/SoftwareProductValidationResultsView.jsx';
import { VSPTestResultsSuccessFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationResultsFactory.js';
import { VSPTestResultKeysFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationResultsFactory.js';
import { VSPTestResultsFailureFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationResultsFactory.js';
import { VSPTestsMapFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPChecksFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';

import TestUtils from 'react-dom/test-utils';

describe('SoftwareProductValidationResults Mapper and View Classes', () => {
     it('mapStateToProps mapper exists', () => {
        expect(mapStateToProps).toBeTruthy();
    });

    it('mapStateToProps fail data test', () => {
        const vspTestResults = VSPTestResultsFailureFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const testResultKeys = VSPTestResultKeysFactory.build();
        const version = {
            name: 1
        };
        const softwareProductId = '1234';
        var testResultToDisplay = {};
        var vspIdAndVer = softwareProductId+version.name;
        testResultToDisplay[vspIdAndVer] = vspTestResults.vspTestResults;
        var testResultKeyByVspId = {};
       testResultKeyByVspId[vspIdAndVer] = testResultKeys.testResultKeys;
        var obj = {
            softwareProduct: {
                softwareProductValidation: {
                       testResultKeys: testResultKeys.testResultKeys
                },
                softwareProductValidationResult: {
                    testResultToDisplay: testResultToDisplay,
                }
            }
        };
        var results = mapStateToProps(obj);
        expect(results.softwareProductValidationResult.testResultToDisplay[vspIdAndVer]).toBeTruthy();
    });

     it('mapStateToProps success data test', () => {
        const vspTestResults = VSPTestResultsSuccessFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const vspChecksList = VSPChecksFactory.build();
        var obj = {
            softwareProduct: {
                softwareProductValidation: {
                },
                softwareProductValidationResult: {
                    vspTestResults: vspTestResults.vspTestResults,
                    vspChecks: vspChecksList,
                    refreshValidationResults: []
                }
            }
        };
        var results = mapStateToProps(obj);
        expect(results.softwareProductValidationResult.vspTestResults).toBeTruthy();
    });

    it('SoftwareProductValidationResultsView test fail render test', () => {
        const vspTestResults = VSPTestResultsFailureFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const testResultKeys = VSPTestResultKeysFactory.build();
        const vspChecksList = VSPChecksFactory.build();

        const version = {
            name: 1
        };
        const softwareProductId = '1234';
        var testResultToDisplay = {};
        var vspIdAndVer = softwareProductId+version.name;
        testResultToDisplay[vspIdAndVer] = vspTestResults.vspTestResults;
        var testResultKeyByVspId = {};
        testResultKeyByVspId[vspIdAndVer] = testResultKeys.testResultKeys;
         var obj = {
                    softwareProductId: softwareProductId,
                    version: version,
                    softwareProductValidation:{
                        testResultKeys: testResultKeys.testResultKeys
                    },
                    softwareProductValidationResult: {
                        testResultToDisplay: testResultToDisplay,
                        vspChecks: vspChecksList,
                         refreshValidationResults: [],
                         testResultKeys: testResultKeyByVspId
                    }
                };

        let vspValidationResultsView = TestUtils.renderIntoDocument(
            <SoftwareProductValidationResultsView {...obj} />
        );
        expect(vspValidationResultsView).toBeTruthy();
    });

    it('SoftwareProductValidationResultsView test success render test', () => {
        const vspTestResults = VSPTestResultsSuccessFactory.build();
        const testResultKeys = VSPTestResultKeysFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const vspChecksList = VSPChecksFactory.build();
        let version = {
            name: 1
        };

        const softwareProductId = '1234';
        var testResultToDisplay = {};
        var vspIdAndVer = softwareProductId+version.name;
        testResultToDisplay[vspIdAndVer] = vspTestResults.vspTestResults;
        var testResultKeyByVspId = {};
         testResultKeyByVspId[vspIdAndVer] = testResultKeys.testResultKeys;

        var obj = {
            softwareProductId: softwareProductId,
            version: version,
            softwareProductValidation:{
                testResultKeys: testResultKeys.testResultKeys
            },
            softwareProductValidationResult: {
                testResultToDisplay: testResultToDisplay,
                vspChecks: vspChecksList
            }
        };
        let vspValidationResultsView = TestUtils.renderIntoDocument(
            <SoftwareProductValidationResultsView {...obj} />
        );
        expect(vspValidationResultsView).toBeTruthy();
    });
});
