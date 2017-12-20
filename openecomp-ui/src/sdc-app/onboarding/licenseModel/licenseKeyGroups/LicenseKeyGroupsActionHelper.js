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
import {actionTypes as licenseKeyGroupsConstants} from './LicenseKeyGroupsConstants.js';
import {actionTypes as limitEditorActions} from 'sdc-app/onboarding/licenseModel/limits/LimitEditorConstants.js';
import {default as getValue, getStrValue} from 'nfvo-utils/getValue.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';

function baseUrl(licenseModelId, version) {
	const restPrefix = Configuration.get('restPrefix');
	const {id: versionId} = version;
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/versions/${versionId}/license-key-groups`;
}

function fetchLicenseKeyGroupsList(licenseModelId, version) {
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}`);
}

function deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId, version) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}`);
}

function postLicenseKeyGroup(licenseModelId, licenseKeyGroup, version) {
	return RestAPIUtil.post(baseUrl(licenseModelId, version), {
		name: licenseKeyGroup.name,
		description: licenseKeyGroup.description,
		operationalScope: getValue(licenseKeyGroup.operationalScope),
		type: licenseKeyGroup.type,
		increments: licenseKeyGroup.increments,
		thresholdValue: licenseKeyGroup.thresholdValue,
		thresholdUnits: getValue(licenseKeyGroup.thresholdUnits),
		startDate: licenseKeyGroup.startDate,
		expiryDate: licenseKeyGroup.expiryDate
	});
}

function putLicenseKeyGroup(licenseModelId, licenseKeyGroup, version) {
	return RestAPIUtil.put(`${baseUrl(licenseModelId, version)}/${licenseKeyGroup.id}`, {
		name: licenseKeyGroup.name,
		description: licenseKeyGroup.description,
		operationalScope: getValue(licenseKeyGroup.operationalScope),
		type: licenseKeyGroup.type,
		increments: licenseKeyGroup.increments,
		thresholdValue: licenseKeyGroup.thresholdValue,
		thresholdUnits: getValue(licenseKeyGroup.thresholdUnits),
		startDate: licenseKeyGroup.startDate,
		expiryDate: licenseKeyGroup.expiryDate
	});
}

function fetchLimitsList(licenseModelId, licenseKeyGroupId, version) {
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits`);
}

function deleteLimit(licenseModelId, licenseKeyGroupId, version, limitId) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits/${limitId}`);
}

function postLimit(licenseModelId, licenseKeyGroupId, version, limit) {
	return RestAPIUtil.post(`${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits`, {
		name: limit.name,
		type: limit.type,
		description: limit.description,
		metric: getStrValue(limit.metric),
		value: limit.value,
		unit: getStrValue(limit.unit),
		aggregationFunction: getValue(limit.aggregationFunction),
		time: getValue(limit.time)
	});
}

function putLimit(licenseModelId, licenseKeyGroupId, version, limit) {

	return RestAPIUtil.put(`${baseUrl(licenseModelId, version)}/${licenseKeyGroupId}/limits/${limit.id}`, {
		name: limit.name,
		type: limit.type,
		description: limit.description,
		metric: getStrValue(limit.metric),
		value: limit.value,
		unit: getStrValue(limit.unit),
		aggregationFunction: getValue(limit.aggregationFunction),
		time: getValue(limit.time)
	});
}

export default {
	fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version}) {
		return fetchLicenseKeyGroupsList(licenseModelId, version).then(response => dispatch({
			type: licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_LIST_LOADED,
			response
		}));
	},

	openLicenseKeyGroupsEditor(dispatch, {licenseKeyGroup, licenseModelId, version} = {}) {
		if (licenseModelId && version) {
			this.fetchLimits(dispatch, {licenseModelId, version, licenseKeyGroup});
		}
		dispatch({
			type: licenseKeyGroupsConstants.licenseKeyGroupsEditor.OPEN,
			licenseKeyGroup
		});
	},

	closeLicenseKeyGroupEditor(dispatch){
		dispatch({
			type: licenseKeyGroupsConstants.licenseKeyGroupsEditor.CLOSE
		});
	},

	saveLicenseKeyGroup(dispatch, {licenseModelId, previousLicenseKeyGroup, licenseKeyGroup, version}) {
		if (previousLicenseKeyGroup) {
			return putLicenseKeyGroup(licenseModelId, licenseKeyGroup, version).then(() => {
				dispatch({
					type: licenseKeyGroupsConstants.EDIT_LICENSE_KEY_GROUP,
					licenseKeyGroup
				});
				ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
			});
		}
		else {
			return postLicenseKeyGroup(licenseModelId, licenseKeyGroup, version).then(response => {
				dispatch({
					type: licenseKeyGroupsConstants.ADD_LICENSE_KEY_GROUP,
					licenseKeyGroup: {
						...licenseKeyGroup,
						referencingFeatureGroups: [],
						id: response.value
					}
				});
				ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
			});
		}


	},

	deleteLicenseKeyGroup(dispatch, {licenseModelId, licenseKeyGroupId, version}){
		return deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId, version).then(()=> {
			dispatch({
				type: licenseKeyGroupsConstants.DELETE_LICENSE_KEY_GROUP,
				licenseKeyGroupId
			});
			ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
		});
	},

	hideDeleteConfirm(dispatch) {
		dispatch({
			type: licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_DELETE_CONFIRM,
			licenseKeyGroupToDelete: false
		});
	},

	openDeleteLicenseAgreementConfirm(dispatch, {licenseKeyGroup}) {
		dispatch({
			type: licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_DELETE_CONFIRM,
			licenseKeyGroupToDelete: licenseKeyGroup
		});
	},


	fetchLimits(dispatch, {licenseModelId, version, licenseKeyGroup}) {
		return fetchLimitsList(licenseModelId, licenseKeyGroup.id, version).then(response => {
			dispatch({
				type: licenseKeyGroupsConstants.licenseKeyGroupsEditor.LIMITS_LIST_LOADED,
				response
			});
		});
	},

	submitLimit(dispatch, {licenseModelId, version, licenseKeyGroup, limit}) {
		const promise = limit.id ? putLimit(licenseModelId,licenseKeyGroup.id, version, limit) :
			 postLimit(licenseModelId,licenseKeyGroup.id, version, limit);
		return promise.then(() => {
			dispatch({
				type: limitEditorActions.CLOSE
			});
			this.fetchLimits(dispatch, {licenseModelId, version, licenseKeyGroup});
			ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
		});
	},

	deleteLimit(dispatch, {licenseModelId, version, licenseKeyGroup, limit}) {
		return deleteLimit(licenseModelId,licenseKeyGroup.id, version, limit.id).then(() => {
			this.fetchLimits(dispatch, {licenseModelId, version, licenseKeyGroup});
			ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
		});
	}


};
