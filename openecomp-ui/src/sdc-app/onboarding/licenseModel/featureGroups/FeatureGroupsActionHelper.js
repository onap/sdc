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

import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {actionTypes as featureGroupsActionConstants} from './FeatureGroupsConstants.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';
import EntitlementPoolsActionHelper from 'sdc-app/onboarding/licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';
import LicenseKeyGroupsActionHelper from 'sdc-app/onboarding/licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';

function baseUrl(licenseModelId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/feature-groups`;
}

function fetchFeatureGroupsList(licenseModelId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId)}${versionQuery}`);
}

function deleteFeatureGroup(licenseModelId, featureGroupId) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId)}/${featureGroupId}`);
}

function addFeatureGroup(licenseModelId, featureGroup) {
	return RestAPIUtil.create(baseUrl(licenseModelId), {
		name: featureGroup.name,
		description: featureGroup.description,
		partNumber: featureGroup.partNumber,
		addedLicenseKeyGroupsIds: featureGroup.licenseKeyGroupsIds,
		addedEntitlementPoolsIds: featureGroup.entitlementPoolsIds
	});
}

function updateFeatureGroup(licenseModelId, previousFeatureGroup, featureGroup) {

	const {licenseKeyGroupsIds = []} = featureGroup;
	const {licenseKeyGroupsIds: prevLicenseKeyGroupsIds = []} = previousFeatureGroup;
	const {entitlementPoolsIds = []} = featureGroup;
	const {entitlementPoolsIds: prevEntitlementPoolsIds = []} = previousFeatureGroup;
	return RestAPIUtil.save(`${baseUrl(licenseModelId)}/${featureGroup.id}`, {
		name: featureGroup.name,
		description: featureGroup.description,
		partNumber: featureGroup.partNumber,
		addedLicenseKeyGroupsIds: licenseKeyGroupsIds.filter(licenseKeyGroupId => prevLicenseKeyGroupsIds.indexOf(licenseKeyGroupId) === -1),
		removedLicenseKeyGroupsIds: prevLicenseKeyGroupsIds.filter(prevLicenseKeyGroupId => licenseKeyGroupsIds.indexOf(prevLicenseKeyGroupId) === -1),
		addedEntitlementPoolsIds: entitlementPoolsIds.filter(entitlementPoolId => prevEntitlementPoolsIds.indexOf(entitlementPoolId) === -1),
		removedEntitlementPoolsIds: prevEntitlementPoolsIds.filter(prevEntitlementPoolId => entitlementPoolsIds.indexOf(prevEntitlementPoolId) === -1)

	});
}

export default {
	fetchFeatureGroupsList(dispatch, {licenseModelId, version}) {
		return fetchFeatureGroupsList(licenseModelId, version).then(response => dispatch({
			type: featureGroupsActionConstants.FEATURE_GROUPS_LIST_LOADED,
			response
		}));
	},

	deleteFeatureGroup(dispatch, {licenseModelId, featureGroupId}) {
		return deleteFeatureGroup(licenseModelId, featureGroupId).then(() => dispatch({
			type: featureGroupsActionConstants.DELETE_FEATURE_GROUPS,
			featureGroupId
		}));
	},

	saveFeatureGroup(dispatch, {licenseModelId, previousFeatureGroup, featureGroup}) {
		if (previousFeatureGroup) {
			return updateFeatureGroup(licenseModelId, previousFeatureGroup, featureGroup).then(() => dispatch({
				type: featureGroupsActionConstants.EDIT_FEATURE_GROUPS,
				featureGroup
			}));
		}
		else {
			return addFeatureGroup(licenseModelId, featureGroup).then(response => dispatch({
				type: featureGroupsActionConstants.ADD_FEATURE_GROUPS,
				featureGroup: {
					...featureGroup,
					id: response.value
				}
			}));
		}
	},

	selectEntitlementPoolsEditorTab(dispatch, {tab}) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.SELECT_TAB,
			tab
		});
	},

	selectFeatureGroupsEditorEntitlementPoolsButtonTab(dispatch, {buttonTab}) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB,
			buttonTab
		});
	},

	selectFeatureGroupsEditorLicenseKeyGroupsButtonTab(dispatch, {buttonTab}) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB,
			buttonTab
		});
	},

	openFeatureGroupsEditor(dispatch, {featureGroup, licenseModelId}) {
		EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId});
		LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId});
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.OPEN,
			featureGroup
		});
	},

	closeFeatureGroupsEditor(dispatch) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.CLOSE
		});
	},

	featureGroupsEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.DATA_CHANGED,
			deltaData
		});
	},

	hideDeleteConfirm(dispatch) {
		dispatch({
			type: featureGroupsActionConstants.FEATURE_GROUPS_DELETE_CONFIRM,
			featureGroupToDelete: false
		});
	},

	openDeleteFeatureGroupConfirm(dispatch, {featureGroup}) {
		dispatch({
			type: featureGroupsActionConstants.FEATURE_GROUPS_DELETE_CONFIRM,
			featureGroupToDelete: featureGroup
		});
	},

	switchVersion(dispatch, {licenseModelId, version}) {
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			this.fetchFeatureGroupsList(dispatch, {licenseModelId, version});
		});
	}
};
