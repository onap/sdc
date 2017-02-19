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
import {actionTypes as licenseKeyGroupsConstants} from './LicenseKeyGroupsConstants.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';

function baseUrl(licenseModelId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/license-key-groups`;
}

function fetchLicenseKeyGroupsList(licenseModelId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId)}${versionQuery}`);
}

function deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId)}/${licenseKeyGroupId}`);
}

function postLicenseKeyGroup(licenseModelId, licenseKeyGroup) {
	return RestAPIUtil.create(baseUrl(licenseModelId), {
		name: licenseKeyGroup.name,
		description: licenseKeyGroup.description,
		operationalScope: licenseKeyGroup.operationalScope,
		type: licenseKeyGroup.type
	});
}

function putLicenseKeyGroup(licenseModelId, licenseKeyGroup) {
	return RestAPIUtil.save(`${baseUrl(licenseModelId)}/${licenseKeyGroup.id}`, {
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

	saveLicenseKeyGroup(dispatch, {licenseModelId, previousLicenseKeyGroup, licenseKeyGroup}) {
		if (previousLicenseKeyGroup) {
			return putLicenseKeyGroup(licenseModelId, licenseKeyGroup).then(() => {
				dispatch({
					type: licenseKeyGroupsConstants.EDIT_LICENSE_KEY_GROUP,
					licenseKeyGroup
				});
			});
		}
		else {
			return postLicenseKeyGroup(licenseModelId, licenseKeyGroup).then(response => {
				dispatch({
					type: licenseKeyGroupsConstants.ADD_LICENSE_KEY_GROUP,
					licenseKeyGroup: {
						...licenseKeyGroup,
						id: response.value
					}
				});
			});
		}


	},

	deleteLicenseKeyGroup(dispatch, {licenseModelId, licenseKeyGroupId}){
		return deleteLicenseKeyGroup(licenseModelId, licenseKeyGroupId).then(()=> {
			dispatch({
				type: licenseKeyGroupsConstants.DELETE_LICENSE_KEY_GROUP,
				licenseKeyGroupId
			});
		});
	},

	licenseKeyGroupEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: licenseKeyGroupsConstants.licenseKeyGroupsEditor.DATA_CHANGED,
			deltaData
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
