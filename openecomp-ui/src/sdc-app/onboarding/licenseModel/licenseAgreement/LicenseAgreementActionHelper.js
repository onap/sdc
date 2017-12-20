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
import {actionTypes as licenseAgreementActionTypes} from './LicenseAgreementConstants.js';
import FeatureGroupsActionHelper from 'sdc-app/onboarding/licenseModel/featureGroups/FeatureGroupsActionHelper.js';
import ItemsHelper from 'sdc-app/common/helpers/ItemsHelper.js';

function baseUrl(licenseModelId, version) {
	const restPrefix = Configuration.get('restPrefix');
	const {id: versionId} = version;
	return `${restPrefix}/v1.0/vendor-license-models/${licenseModelId}/versions/${versionId}/license-agreements`;
}

function fetchLicenseAgreementList(licenseModelId, version) {
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}`);
}

function fetchLicenseAgreement(licenseModelId, licenseAgreementId, version) {
	return RestAPIUtil.fetch(`${baseUrl(licenseModelId, version)}/${licenseAgreementId}`);
}

function postLicenseAgreement(licenseModelId, licenseAgreement, version) {
	return RestAPIUtil.post(baseUrl(licenseModelId, version), {
		name: licenseAgreement.name,
		description: licenseAgreement.description,
		licenseTerm: licenseAgreement.licenseTerm,
		requirementsAndConstrains: licenseAgreement.requirementsAndConstrains,
		addedFeatureGroupsIds: licenseAgreement.featureGroupsIds
	});
}

function putLicenseAgreement(licenseModelId, previousLicenseAgreement, licenseAgreement, version) {
	const {featureGroupsIds = []} = licenseAgreement;
	const {featureGroupsIds: prevFeatureGroupsIds = []} = previousLicenseAgreement;
	return RestAPIUtil.put(`${baseUrl(licenseModelId, version)}/${licenseAgreement.id}`, {
		name: licenseAgreement.name,
		description: licenseAgreement.description,
		licenseTerm: licenseAgreement.licenseTerm,
		requirementsAndConstrains: licenseAgreement.requirementsAndConstrains,
		addedFeatureGroupsIds: featureGroupsIds.filter(featureGroupId => prevFeatureGroupsIds.indexOf(featureGroupId) === -1),
		removedFeatureGroupsIds: prevFeatureGroupsIds.filter(prevFeatureGroupsId => featureGroupsIds.indexOf(prevFeatureGroupsId) === -1)
	});
}

function deleteLicenseAgreement(licenseModelId, licenseAgreementId, version) {
	return RestAPIUtil.destroy(`${baseUrl(licenseModelId, version)}/${licenseAgreementId}`);
}

export default {

	fetchLicenseAgreementList(dispatch, {licenseModelId, version}) {
		return fetchLicenseAgreementList(licenseModelId, version).then(response => dispatch({
			type: licenseAgreementActionTypes.LICENSE_AGREEMENT_LIST_LOADED,
			response
		}));
	},

	fetchLicenseAgreement(dispatch, {licenseModelId, licenseAgreementId, version}) {
		return fetchLicenseAgreement(licenseModelId, licenseAgreementId, version);
	},

	openLicenseAgreementEditor(dispatch, {licenseModelId, licenseAgreement, version}) {
		FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version});
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.OPEN,
			licenseAgreement
		});
	},

	closeLicenseAgreementEditor(dispatch) {
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.CLOSE
		});
	},


	saveLicenseAgreement(dispatch, {licenseModelId, previousLicenseAgreement, licenseAgreement, version}) {
		if (previousLicenseAgreement) {
			return putLicenseAgreement(licenseModelId, previousLicenseAgreement, licenseAgreement, version).then(() => {
				this.fetchLicenseAgreementList(dispatch, {licenseModelId, version});
				ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
			});
		}
		else {
			return postLicenseAgreement(licenseModelId, licenseAgreement, version).then(() => {
				this.fetchLicenseAgreementList(dispatch, {licenseModelId, version});
				FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version});
				ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
			});
		}
	},

	deleteLicenseAgreement(dispatch, {licenseModelId, licenseAgreementId, version}) {
		return deleteLicenseAgreement(licenseModelId, licenseAgreementId, version).then(() => {
			dispatch({
				type: licenseAgreementActionTypes.DELETE_LICENSE_AGREEMENT,
				licenseAgreementId
			});
			ItemsHelper.checkItemStatus(dispatch, {itemId: licenseModelId, versionId: version.id});
		});
	},

	selectLicenseAgreementEditorTab(dispatch, {tab}) {
		dispatch({
			type: licenseAgreementActionTypes.licenseAgreementEditor.SELECT_TAB,
			tab
		});
	}
};
