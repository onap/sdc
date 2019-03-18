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
import { actionTypes } from './SoftwareProductValidationConstants.js';

export default (state = {}, action) => {
    switch (action.type) {
        case actionTypes.POST_VSP_TESTS:
            return {
                ...state,
                vspTestResults: action.vspTestResults
            };
        case actionTypes.FETCH_VSP_CHECKS:
            return {
                ...state,
                vspChecks: action.vspChecks
            };
        case actionTypes.SET_ACTIVE_TAB:
            return { ...state, activeTab: action.activeTab };
        case actionTypes.SET_VSP_TESTS_MAP:
            return {
                ...state,
                vspTestsMap: action.vspTestsMap
            };
        case actionTypes.SET_COMPLIANCE_CHECKED:
            return {
                ...state,
                complianceChecked: action.complianceChecked
            };
        case actionTypes.SET_CERTIFICATION_CHECKED:
            return {
                ...state,
                certificationChecked: action.certificationChecked
            };
        case actionTypes.SET_TESTS_REQUEST:
            return {
                ...state,
                testsRequest: action.testsRequest,
                generalInfo: action.generalInfo
            };
        case actionTypes.SET_GENERAL_INFO:
            return {
                ...state,
                generalInfo: action.generalInfo
            };
        default:
            return state;
    }
};
