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
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {actionTypes as featureGroupsActionConstants} from './FeatureGroupsConstants.js';
import EntitlementPoolsActionHelper from 'sdc-app/onboarding/licenseModel/entitlementPools/EntitlementPoolsActionHelper.js';
import LicenseKeyGroupsActionHelper from 'sdc-app/onboarding/licenseModel/licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';

function baseUrl(licenseModelId, version) {
	const restPrefix = Configuration.get('restPrefix');
	const {id: versionId} = version;
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/versions/${versionId}/feature-groups`;
}

function fetchFeatureGroup(licenseModelId, featureGroupId, version) {
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}/${featureGroupId}`);
}

function fetchFeatureGroupsList(licenseModelId, version) {
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}`);
}

function deleteFeatureGroup(licenseModelId, featureGroupId, version) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId, version)}/${featureGroupId}`);
}

function addFeatureGroup(licenseModelId, featureGroup, version) {
	return RestAPIUtil.post(baseUrl(licenseModelId, version), {
		name: featureGroup.name,
		description: featureGroup.description,
		partNumber: featureGroup.partNumber,
		manufacturerReferenceNumber: featureGroup.manufacturerReferenceNumber,
		addedLicenseKeyGroupsIds: featureGroup.licenseKeyGroupsIds,
		addedEntitlementPoolsIds: featureGroup.entitlementPoolsIds
	});
}

function updateFeatureGroup(licenseModelId, previousFeatureGroup, featureGroup, version) {

	const {licenseKeyGroupsIds = []} = featureGroup;
	const {licenseKeyGroupsIds: prevLicenseKeyGroupsIds = []} = previousFeatureGroup;
	const {entitlementPoolsIds = []} = featureGroup;
	const {entitlementPoolsIds: prevEntitlementPoolsIds = []} = previousFeatureGroup;
	return RestAPIUtil.put(`${baseUrl(licenseModelId, version)}/${featureGroup.id}`, {
		name: featureGroup.name,
		description: featureGroup.description,
		partNumber: featureGroup.partNumber,
		manufacturerReferenceNumber: featureGroup.manufacturerReferenceNumber,
		addedLicenseKeyGroupsIds: licenseKeyGroupsIds.filter(licenseKeyGroupId => prevLicenseKeyGroupsIds.indexOf(licenseKeyGroupId) === -1),
		removedLicenseKeyGroupsIds: prevLicenseKeyGroupsIds.filter(prevLicenseKeyGroupId => licenseKeyGroupsIds.indexOf(prevLicenseKeyGroupId) === -1),
		addedEntitlementPoolsIds: entitlementPoolsIds.filter(entitlementPoolId => prevEntitlementPoolsIds.indexOf(entitlementPoolId) === -1),
		removedEntitlementPoolsIds: prevEntitlementPoolsIds.filter(prevEntitlementPoolId => entitlementPoolsIds.indexOf(prevEntitlementPoolId) === -1)

	});
}

export default {
	fetchFeatureGroup(dispatch, {licenseModelId, featureGroupId, version}) {
		return fetchFeatureGroup(licenseModelId, featureGroupId, version);
	},

	fetchFeatureGroupsList(dispatch, {licenseModelId, version}) {
		return fetchFeatureGroupsList(licenseModelId, version).then(response => dispatch({
			type: featureGroupsActionConstants.FEATURE_GROUPS_LIST_LOADED,
			response
		}));
	},

	deleteFeatureGroup(dispatch, {licenseModelId, featureGroupId, version}) {
		return deleteFeatureGroup(licenseModelId, featureGroupId, version).then(() => {
			dispatch({
				type: featureGroupsActionConstants.DELETE_FEATURE_GROUPS,
				featureGroupId
			});
			ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
		});
	},

	saveFeatureGroup(dispatch, {licenseModelId, previousFeatureGroup, featureGroup, version}) {
		if (previousFeatureGroup) {
			return updateFeatureGroup(licenseModelId, previousFeatureGroup, featureGroup, version).then(() =>{
				dispatch({
					type: featureGroupsActionConstants.EDIT_FEATURE_GROUPS,
					featureGroup
				});
				EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version});
				LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version});
				ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
			});
		}
		else {
			return addFeatureGroup(licenseModelId, featureGroup, version).then(response => {
				dispatch({
					type: featureGroupsActionConstants.ADD_FEATURE_GROUPS,
					featureGroup: {
						...featureGroup,
						id: response.value,
						referencingLicenseAgreements: []
					}
				});
				EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version});
				LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version});
				ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
			});
		}
	},

	selectEntitlementPoolsEditorTab(dispatch, {tab}) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.SELECT_TAB,
			tab
		});
	},

	openFeatureGroupsEditor(dispatch, {featureGroup, licenseModelId, version}) {
		return Promise.all([
			EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version}),
			LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version})
		]).then(() => {
			dispatch({
				type: featureGroupsActionConstants.featureGroupsEditor.OPEN,
				featureGroup
			});
		});
	},

	closeFeatureGroupsEditor(dispatch) {
		dispatch({
			type: featureGroupsActionConstants.featureGroupsEditor.CLOSE
		});
	}
};
