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
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';

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
		operationalScope: licenseKeyGroup.operationalScope,
		type: licenseKeyGroup.type
	});
}

function putLicenseKeyGroup(licenseModelId, licenseKeyGroup, version) {
	return RestAPIUtil.put(`${baseUrl(licenseModelId, version)}/${licenseKeyGroup.id}`, {
		name: licenseKeyGroup.name,
		description: licenseKeyGroup.description,
		operationalScope: licenseKeyGroup.operationalScope,
		type: licenseKeyGroup.type
	});
}


export default {
	fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version}) {
		return fetchLicenseKeyGroupsList(licenseModelId, version).then(response => dispatch({
			type: licenseKeyGroupsConstants.LICENSE_KEY_GROUPS_LIST_LOADED,
			response
		}));
	},

	openLicenseKeyGroupsEditor(dispatch, {licenseKeyGroup} = {}) {
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
			});
		}


	},

	deleteLicenseKeyGroup(dispatch, {licenseModelId, licenseKeyGroupId, version}){
		return deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId, version).then(()=> {
			dispatch({
				type: licenseKeyGroupsConstants.DELETE_LICENSE_KEY_GROUP,
				licenseKeyGroupId
			});
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

	switchVersion(dispatch, {licenseModelId, version}) {
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			this.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version});
		});
	}
};
