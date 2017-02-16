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
import {actionTypes as licenseAgreementActionTypes} from './LicenseAgreementConstants.js';
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';

function baseUrl(licenseModelId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/license-agreements`;
}

function fetchLicenseAgreementList(licenseModelId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId)}${versionQuery}`);
}

function postLicenseAgreement(licenseModelId, licenseAgreement) {
	return RestAPIUtil.create(baseUrl(licenseModelId), {
		name: licenseAgreement.name,
		description: licenseAgreement.description,
		licenseTerm: licenseAgreement.licenseTerm,
		requirementsAndConstrains: licenseAgreement.requirementsAndConstrains,
		addedFeatureGroupsIds: licenseAgreement.featureGroupsIds
	});
}

function putLicenseAgreement(licenseModelId, previousLicenseAgreement, licenseAgreement) {
	const {featureGroupsIds = []} = licenseAgreement;
	const {featureGroupsIds: prevFeatureGroupsIds = []} = previousLicenseAgreement;
	return RestAPIUtil.save(`${baseUrl(licenseModelId)}/${licenseAgreement.id}`, {
		name: licenseAgreement.name,
		description: licenseAgreement.description,
		licenseTerm: licenseAgreement.licenseTerm,
		requirementsAndConstrains: licenseAgreement.requirementsAndConstrains,
		addedFeatureGroupsIds: featureGroupsIds.filter(featureGroupId => prevFeatureGroupsIds.indexOf(featureGroupId) === -1),
		removedFeatureGroupsIds: prevFeatureGroupsIds.filter(prevFeatureGroupsId => featureGroupsIds.indexOf(prevFeatureGroupsId) === -1)
	});
}

function deleteLicenseAgreement(licenseModelId, licenseAgreementId) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId)}/${licenseAgreementId}`);
}

export default {

	fetchLicenseAgreementList(dispatch, {licenseModelId, version}) {
		return fetchLicenseAgreementList(licenseModelId, version).then(response => dispatch({
			type: licenseAgreementActionTypes.LICENSE_AGREEMENT_LIST_LOADED,
			response
		}));
	},

	openLicenseAgreementEditor(dispatch, {licenseModelId, licenseAgreement}) {
		FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId});
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.OPEN,
			licenseAgreement
		});
	},

	licenseAgreementEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.DATA_CHANGED,
			deltaData
		});
	},

	closeLicenseAgreementEditor(dispatch) {
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.CLOSE
		});
	},


	saveLicenseAgreement(dispatch, {licenseModelId, previousLicenseAgreement, licenseAgreement}) {
		if (previousLicenseAgreement) {
			return putLicenseAgreement(licenseModelId, previousLicenseAgreement, licenseAgreement).then(() => {
				dispatch({
					type: licenseAgreementActionTypes.EDIT_LICENSE_AGREEMENT,
					licenseAgreement
				});
			});
		}
		else {
			return postLicenseAgreement(licenseModelId, licenseAgreement).then(response => {
				dispatch({
					type: licenseAgreementActionTypes.ADD_LICENSE_AGREEMENT,
					licenseAgreement: {
						...licenseAgreement,
						id: response.value
					}
				});
			});
		}
	},

	deleteLicenseAgreement(dispatch, {licenseModelId, licenseAgreementId}) {
		return deleteLicenseAgreement(licenseModelId, licenseAgreementId).then(() => {
			dispatch({
				type: licenseAgreementActionTypes.DELETE_LICENSE_AGREEMENT,
				licenseAgreementId
			});
		});
	},

	selectLicenseAgreementEditorTab(dispatch, {tab}) {
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.SELECT_TAB,
			tab
		});
	},

	selectLicenseAgreementEditorFeatureGroupsButtonTab(dispatch, {buttonTab}) {
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.SELECT_FEATURE_GROUPS_BUTTONTAB,
			buttonTab
		});
	},

	hideDeleteConfirm(dispatch) {
		dispatch({
			type: licenseAgreementActionTypes.LICENSE_AGREEMENT_DELETE_CONFIRM,
			licenseAgreementToDelete: false
		});
	},

	openDeleteLicenseAgreementConfirm(dispatch, {licenseAgreement} ) {
		dispatch({
			type: licenseAgreementActionTypes.LICENSE_AGREEMENT_DELETE_CONFIRM,
			licenseAgreementToDelete: licenseAgreement
		});
	},

	switchVersion(dispatch, {licenseModelId, version}) {
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version}).then(() => {
			this.fetchLicenseAgreementList(dispatch, {licenseModelId, version});
		});
	}
};

