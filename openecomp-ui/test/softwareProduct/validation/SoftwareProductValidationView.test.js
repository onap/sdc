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
import { Provider } from 'react-redux';
import { storeCreator } from 'sdc-app/AppStore.js';

import { mapStateToProps } from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidation.js';
import { mapActionsToProps } from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidation.js';
import SoftwareProductValidationView from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidationView.jsx';
import { VSPComplianceCheckedFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPCertificationCheckedFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPChecksFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { VSPTestsMapFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationFactory.js';
import { tabsMapping } from 'sdc-app/onboarding/softwareProduct/validation/SoftwareProductValidationConstants.js';
import TestUtils from 'react-dom/test-utils';
//import { scryRenderedDOMComponentsWithTestId } from 'test-utils/Util.js';

describe('SoftwareProductValidation Mapper and View Classes', () => {
    it('mapStateToProps mapper exists', () => {
        expect(mapStateToProps).toBeTruthy();
    });

    it('mapActionsToProps mapper exists', () => {
        expect(mapActionsToProps).toBeTruthy();
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
                    complianceChecked: complianceChecked,
                    activeTab: tabsMapping.SETUP
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
        expect(results.softwareProductValidation.activeTab).toBeTruthy();
    });

    it('SoftwareProductValidationView render test', () => {
        const vspChecksList = VSPChecksFactory.build();
        const vspTestsMap = VSPTestsMapFactory.build();
        const certificationChecked = VSPCertificationCheckedFactory.build();
        const complianceChecked = VSPComplianceCheckedFactory.build();
        let dummyFunc = () => {};
        const version = {
            name: 1
        };
        const softwareProductId = '1234';
        const status = 'draft';
        var obj = {
            version: version,
            softwareProductId: softwareProductId,
            status: status,
            softwareProductValidation: {
                vspChecks: vspChecksList,
                vspTestsMap: vspTestsMap,
                certificationChecked: certificationChecked.certificationChecked,
                complianceChecked: complianceChecked.complianceChecked,
                activeTab: tabsMapping.SETUP
            }
        };
        const store = storeCreator();
        let softwareProductValidationView = TestUtils.renderIntoDocument(
            <Provider store={store}>
                <SoftwareProductValidationView
                    {...obj}
                    onErrorThrown={dummyFunc}
                    onTestSubmit={dummyFunc}
                    setVspTestsMap={dummyFunc}
                    setActiveTab={dummyFunc}
                    setComplianceChecked={dummyFunc}
                    setCertificationChecked={dummyFunc}
                />
            </Provider>
        );

        expect(softwareProductValidationView).toBeTruthy();

        // let goToInput = scryRenderedDOMComponentsWithTestId(
        //     softwareProductValidationView,
        //     'go-to-inputs'
        // );
        // expect(goToInput).toBeTruthy();
        // TestUtils.Simulate.click(goToInput[0]);
        // let goToInput = TestUtils.findRenderedDOMComponentWithClass(
        //     softwareProductValidationView,
        //     'go-to-inputs-btn'
        // );
        // TestUtils.Simulate.click(goToInput);
    });
});
