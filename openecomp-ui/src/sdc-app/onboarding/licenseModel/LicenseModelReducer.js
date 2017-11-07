/*!
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
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
import {combineReducers} from 'redux';

import activityLogReducer from 'sdc-app/common/activity-log/ActivityLogReducer.js';

import licenseModelCreationReducer from './creation/LicenseModelCreationReducer.js';
import licenseModelEditorReducer from './LicenseModelEditorReducer.js';

import licenseAgreementListReducer from './licenseAgreement/LicenseAgreementListReducer.js';
import licenseAgreementEditorReducer from './licenseAgreement/LicenseAgreementEditorReducer.js';

import featureGroupsEditorReducer from './featureGroups/FeatureGroupsEditorReducer.js';
import featureGroupsListReducer from './featureGroups/FeatureGroupsListReducer.js';

import entitlementPoolsListReducer from './entitlementPools/EntitlementPoolsListReducer.js';
import entitlementPoolsEditorReducer from './entitlementPools/EntitlementPoolsEditorReducer.js';

import licenseKeyGroupsEditorReducer from './licenseKeyGroups/LicenseKeyGroupsEditorReducer.js';
import licenseKeyGroupsListReducer from './licenseKeyGroups/LicenseKeyGroupsListReducer.js';

import {createPlainDataReducer} from 'sdc-app/common/reducers/PlainDataReducer.js';

import {actionTypes as licenseModelOverviewConstants, VLM_DESCRIPTION_FORM} from './overview/LicenseModelOverviewConstants.js';
import limitEditorReducer from './limits/LimitEditorReducer.js'; 

export default combineReducers({
	licenseModelCreation: createPlainDataReducer(licenseModelCreationReducer),
	licenseModelEditor: licenseModelEditorReducer,

	licenseAgreement: combineReducers({
		licenseAgreementEditor: createPlainDataReducer(licenseAgreementEditorReducer),
		licenseAgreementList: licenseAgreementListReducer
	}),
	featureGroup: combineReducers({
		featureGroupEditor: createPlainDataReducer(featureGroupsEditorReducer),
		featureGroupsList: featureGroupsListReducer
	}),
	entitlementPool: combineReducers({
		entitlementPoolEditor: createPlainDataReducer(entitlementPoolsEditorReducer),
		entitlementPoolsList: entitlementPoolsListReducer
	}),
	licenseKeyGroup: combineReducers({
		licenseKeyGroupsEditor: createPlainDataReducer(licenseKeyGroupsEditorReducer),
		licenseKeyGroupsList: licenseKeyGroupsListReducer
	}),
	licenseModelOverview: combineReducers({
		selectedTab: (state = null, action) => action.type === licenseModelOverviewConstants.LICENSE_MODEL_OVERVIEW_TAB_SELECTED ? action.buttonTab : state,
		descriptionEditor: createPlainDataReducer(function(state = false, action) {
			if (action.type === licenseModelOverviewConstants.LM_DATA_CHANGED) {
				return {
					...state,
					data : {
						description : action.description
					},
					formReady: null,
					formName: VLM_DESCRIPTION_FORM,
					genericFieldInfo: {
						'description': {
							isValid: true,
							errorText: '',
							validations: [{type: 'required', data: true}, {type: 'maxLength', data: 1000}]
						}
					}
				};
				//return action.description;
			} else {
				return state;
			}
		}
	)}),
	limitEditor: createPlainDataReducer(limitEditorReducer),
	activityLog: activityLogReducer
});
