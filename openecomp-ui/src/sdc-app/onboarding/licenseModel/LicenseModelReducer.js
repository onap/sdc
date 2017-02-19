/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

import {combineReducers} from 'redux';

import licenseModelCreationReducer from './creation/LicenseModelCreationReducer.js';
import licenseModelEditorReducer from './LicenseModelEditorReducer.js';

import licenseAgreementListReducer from './licenseAgreement/LicenseAgreementListReducer.js';
import licenseAgreementEditorReducer from './licenseAgreement/LicenseAgreementEditorReducer.js';
import {actionTypes as licenseAgreementActionTypes} from './licenseAgreement/LicenseAgreementConstants.js';

import featureGroupsEditorReducer from './featureGroups/FeatureGroupsEditorReducer.js';
import featureGroupsListReducer from './featureGroups/FeatureGroupsListReducer.js';
import {actionTypes as featureGroupsActionConstants} from './featureGroups/FeatureGroupsConstants';

import entitlementPoolsListReducer from './entitlementPools/EntitlementPoolsListReducer.js';
import entitlementPoolsEditorReducer from './entitlementPools/EntitlementPoolsEditorReducer.js';
import {actionTypes as entitlementPoolsConstants} from './entitlementPools/EntitlementPoolsConstants';

import licenseKeyGroupsEditorReducer from './licenseKeyGroups/LicenseKeyGroupsEditorReducer.js';
import licenseKeyGroupsListReducer from './licenseKeyGroups/LicenseKeyGroupsListReducer.js';
import {actionTypes as licenseKeyGroupsConstants} from './licenseKeyGroups/LicenseKeyGroupsConstants.js';

export default combineReducers({
	licenseModelCreation: licenseModelCreationReducer,
	licenseModelEditor: licenseModelEditorReducer,

	licenseAgreement: combineReducers({
		licenseAgreementEditor: licenseAgreementEditorReducer,
		licenseAgreementList: licenseAgreementListReducer,
		licenseAgreementToDelete: (state = false, action) => action.type === licenseAgreementActionTypes.LICENSE_AGREEMENT_DELETE_CONFIRM ? action.licenseAgreementToDelete : state
	}),
	featureGroup: combineReducers({
		featureGroupEditor: featureGroupsEditorReducer,
		featureGroupsList: featureGroupsListReducer,
		featureGroupToDelete: (state = false, action) => action.type === featureGroupsActionConstants.FEATURE_GROUPS_DELETE_CONFIRM ? action.featureGroupToDelete : state
	}),
	entitlementPool: combineReducers({
		entitlementPoolEditor: entitlementPoolsEditorReducer,
		entitlementPoolsList: entitlementPoolsListReducer,
		entitlementPoolToDelete: (state = false, action) => action.type === entitlementPoolsConstants.ENTITLEMENT_POOLS_DELETE_CONFIRM ? action.entitlementPoolToDelete : state
	}),
	licenseKeyGroup: combineReducers({
		licenseKeyGroupsEditor: licenseKeyGroupsEditorReducer,
		licenseKeyGroupsList: licenseKeyGroupsListReducer,
		licenseKeyGroupToDelete: (state = false, action) => action.type === licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_DELETE_CONFIRM ? action.licenseKeyGroupToDelete : state
	}),
});
