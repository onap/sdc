/**
 * Copyright (c) 2019 Vodafone Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { connect } from 'react-redux';
import SoftwareProductValidationView from './SoftwareProductValidationView.jsx';
import SoftwareProductValidationActionHelper from './SoftwareProductValidationActionHelper.js';

export const mapStateToProps = ({ softwareProduct }) => {
    let { softwareProductValidation } = softwareProduct;
    return {
        softwareProductValidation
    };
};

export const mapActionsToProps = dispatch => {
    return {
        onErrorThrown: msg => {
            SoftwareProductValidationActionHelper.onErrorThrown(dispatch, msg);
        },

        onTestSubmit: (softwareProductId, version, status, tests) => {
            SoftwareProductValidationActionHelper.navigateToSoftwareProductValidationResults(
                dispatch,
                {
                    softwareProductId,
                    version,
                    status,
                    tests
                }
            );
        },

        setVspTestsMap: map => {
            SoftwareProductValidationActionHelper.setVspTestsMap(dispatch, map);
        },

        setActiveTab: activeTab => {
            SoftwareProductValidationActionHelper.setActiveTab(
                dispatch,
                activeTab
            );
        },

        setComplianceChecked: ({ checked }) => {
            SoftwareProductValidationActionHelper.setComplianceChecked(
                dispatch,
                checked
            );
        },

        setCertificationChecked: ({ checked }) => {
            SoftwareProductValidationActionHelper.setCertificationChecked(
                dispatch,
                checked
            );
        },

        setTestsRequest: (request, info) => {
            SoftwareProductValidationActionHelper.setTestsRequest(
                dispatch,
                request,
                info
            );
        },

        setGeneralInfo: info => {
            SoftwareProductValidationActionHelper.setGeneralInfo(
                dispatch,
                info
            );
        }
    };
};

export default connect(mapStateToProps, mapActionsToProps, null, {
    withRef: true
})(SoftwareProductValidationView);
