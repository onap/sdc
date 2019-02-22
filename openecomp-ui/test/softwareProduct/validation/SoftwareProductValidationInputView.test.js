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
import { mapStateToProps } from 'sdc-app/onboarding/softwareProduct/validation/inputs/VspValidationInputs.js';

import VspValidationInputsView from 'sdc-app/onboarding/softwareProduct/validation/inputs/VspValidationInputsView.jsx';
import TestUtils from 'react-dom/test-utils';

import { VSPComplianceCheckedFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPCertificationCheckedFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPChecksFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPTestsMapFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';

describe('SoftwareProductValidation Mapper and View Classes', () => {
    it('mapStateToProps mapper exists', () => {
        expect(mapStateToProps).toBeTruthy();
    });

    it('mapStateToProps data test', () => {
        const vspChecksList = VSPChecksFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const certificationChecked = VSPCertificationCheckedFactory.build();
        const complianceChecked = VSPComplianceCheckedFactory.build();

        var obj = {
            softwareProduct: {
                softwareProductValidation: {
                    vspChecks: vspChecksList,
                    vspTestsMap: vspTestsMap,
                    certificationChecked: certificationChecked,
                    complianceChecked: complianceChecked
                }
            }
        };
        var results = mapStateToProps(obj);
        expect(results.softwareProductValidation.vspChecks).toBeTruthy();
        expect(results.softwareProductValidation.vspTestsMap).toBeTruthy();
        expect(
            results.softwareProductValidation.certificationChecked
        ).toBeTruthy();
        expect(
            results.softwareProductValidation.complianceChecked
        ).toBeTruthy();
    });

    it('SoftwareProductValidationInputView render test', () => {
        const complianceChecked = VSPComplianceCheckedFactory.build();
        const certificationChecked = VSPCertificationCheckedFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const vspChecksList = VSPChecksFactory.build();

        const version = {
            name: 1
        };
        const softwareProductId = '1234';
        const status = 'draft';
        let tests = [
            {
                testId: 'certquery',
                parameterValues: [
                    {
                        id: 'vspId',
                        value: softwareProductId
                    },
                    {
                        id: 'vspVersion',
                        value: version.name
                    }
                ]
            }
        ];
        var obj = {
            version: version,
            softwareProductId: softwareProductId,
            status: status,
            tests: tests,
            softwareProductValidation: {
                complianceChecked: complianceChecked.complianceChecked,
                certificationChecked: certificationChecked.certificationChecked,
                vspTestsMap: vspTestsMap,
                vspChecks: vspChecksList
            }
        };

        let vspValidationInputView = TestUtils.renderIntoDocument(
            <VspValidationInputsView {...obj} />
        );
        expect(vspValidationInputView).toBeTruthy();
    });
});
