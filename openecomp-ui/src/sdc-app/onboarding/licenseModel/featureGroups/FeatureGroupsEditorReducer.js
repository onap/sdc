/*!
 * Copyright © 2016-2018 European Support Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

import {
    actionTypes,
    FG_EDITOR_FORM,
    state as FeatureGroupStateConstants
} from './FeatureGroupsConstants.js';

export default (state = {}, action) => {
    switch (action.type) {
        case actionTypes.featureGroupsEditor.OPEN:
            return {
                ...state,
                data: action.featureGroup || {},
                formReady: null,
                formName: FG_EDITOR_FORM,
                genericFieldInfo: {
                    description: {
                        isValid: true,
                        errorText: '',
                        validations: [
                            { type: 'maxLength', data: 1000 },
                            { type: 'validateName', data: true }
                        ],
                        tabId:
                            FeatureGroupStateConstants
                                .SELECTED_FEATURE_GROUP_TAB.GENERAL
                    },
                    partNumber: {
                        isValid: true,
                        errorText: '',
                        validations: [
                            { type: 'required', data: true },
                            { type: 'validateName', data: true }
                        ],
                        tabId:
                            FeatureGroupStateConstants
                                .SELECTED_FEATURE_GROUP_TAB.GENERAL
                    },
                    name: {
                        isValid: true,
                        errorText: '',
                        validations: [
                            { type: 'required', data: true },
                            { type: 'maxLength', data: 120 },
                            { type: 'validateName', data: true }
                        ],
                        tabId:
                            FeatureGroupStateConstants
                                .SELECTED_FEATURE_GROUP_TAB.GENERAL
                    }
                }
            };
        case actionTypes.featureGroupsEditor.CLOSE:
            return {};
        case actionTypes.featureGroupsEditor.SELECT_TAB:
            return {
                ...state,
                selectedTab: action.tab
            };

        case actionTypes.featureGroupsEditor
            .SELECTED_ENTITLEMENT_POOLS_BUTTONTAB:
            return {
                ...state,
                selectedEntitlementPoolsButtonTab: action.buttonTab
            };
        case actionTypes.featureGroupsEditor
            .SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB:
            return {
                ...state,
                selectedLicenseKeyGroupsButtonTab: action.buttonTab
            };
        default:
            return state;
    }
};
