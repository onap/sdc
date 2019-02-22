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
import { mapStateToProps } from 'sdc-app/onboarding/softwareProduct/validationMonitor/SoftwareProductValidationMonitor.js';
import SoftwareProductValidationMonitorView from 'sdc-app/onboarding/softwareProduct/validationMonitor/SoftwareProductValidationMonitorView.jsx';
import ShallowRenderer from 'react-test-renderer/shallow';
import { VSPTestResultsSuccessFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationMonitorFactory.js';
import { VSPTestResultsFailureFactory } from 'test-utils/factories/softwareProduct/SoftwareProductValidationMonitorFactory.js';

describe('SoftwareProductValidationMonitor Mapper and View Classes', () => {
    it('mapStateToProps mapper exists', () => {
        expect(mapStateToProps).toBeTruthy();
    });

    it('mapStateToProps fail data test', () => {
        const vspTestResults = VSPTestResultsFailureFactory.build();

        var obj = {
            softwareProduct: {
                softwareProductValidation: {
                    vspTestResults: vspTestResults
                }
            }
        };
        var results = mapStateToProps(obj);
        expect(results.softwareProductValidation.vspTestResults).toBeTruthy();
    });

    it('mapStateToProps success data test', () => {
        const vspTestResults = VSPTestResultsSuccessFactory.build();

        var obj = {
            softwareProduct: {
                softwareProductValidation: {
                    vspTestResults: vspTestResults
                }
            }
        };
        var results = mapStateToProps(obj);
        expect(results.softwareProductValidation.vspTestResults).toBeTruthy();
    });

    it('SoftwareProductValidationMonitorView test fail render test', () => {
        const vspTestResults = VSPTestResultsFailureFactory.build();

        const version = {
            name: 1
        };
        const softwareProductId = '1234';
        const renderer = new ShallowRenderer();
        var obj = {
            softwareProductId: softwareProductId,
            version: version,
            softwareProductValidation: {
                vspTestResults: vspTestResults
            }
        };
        renderer.render(<SoftwareProductValidationMonitorView {...obj} />);
        var renderedOutput = renderer.getRenderOutput();
        expect(renderedOutput).toBeTruthy();
    });

    it('SoftwareProductValidationMonitorView test success render test', () => {
        const vspTestResults = VSPTestResultsSuccessFactory.build();

        let version = {
            name: 1
        };
        const softwareProductId = '1234';
        var obj = {
            softwareProductId: softwareProductId,
            version: version,
            softwareProductValidation: {
                vspTestResults: vspTestResults
            }
        };
        const renderer = new ShallowRenderer();
        renderer.render(<SoftwareProductValidationMonitorView {...obj} />);
        var renderedOutput = renderer.getRenderOutput();
        expect(renderedOutput).toBeTruthy();
    });
});
