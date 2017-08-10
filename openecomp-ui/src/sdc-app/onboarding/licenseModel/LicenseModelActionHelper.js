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
import {actionTypes} from './LicenseModelConstants.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';
import {actionsEnum as vcActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import LicenseAgreementActionHelper from './licenseAgreement/LicenseAgreementActionHelper.js';
import FeatureGroupsActionHelper from './featureGroups/FeatureGroupsActionHelper.js';
import EntitlementPoolsActionHelper from './entitlementPools/EntitlementPoolsActionHelper.js';
import LicenseKeyGroupsActionHelper from './licenseKeyGroups/LicenseKeyGroupsActionHelper.js';
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';

function baseUrl() {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-license-models/`;
}

function fetchLicenseModels() {
	return RestAPIUtil.fetch(baseUrl());
}

function fetchFinalizedLicenseModels() {
	return RestAPIUtil.fetch(`${baseUrl()}?versionFilter=Final`);
}

function fetchLicenseModelById(licenseModelId, version) {
	const {id: versionId} = version;
	return RestAPIUtil.fetch(`${baseUrl()}${licenseModelId}/versions/${versionId}`);
}

function putLicenseModelAction(id, action, version) {
	const {id: versionId} = version;
	return RestAPIUtil.put(`${baseUrl()}${id}/versions/${versionId}/actions`, {action: action});
}

function putLicenseModel(licenseModel) {
	let {id, vendorName, description, iconRef, version: {id: versionId}} = licenseModel;
	return RestAPIUtil.put(`${baseUrl()}${id}/versions/${versionId}`, {
		vendorName,
		description,
		iconRef
	});
}

function adjustMinorVersion(version, value) {
	let ar = version.split('.');
	return ar[0] + '.' + (parseInt(ar[1]) + value);
}

function adjustMajorVersion(version, value) {
	let ar = version.split('.');
	return (parseInt(ar[0]) + value) + '.0';
}

const LicenseModelActionHelper = {

	fetchLicenseModels(dispatch) {
		return fetchLicenseModels().then(response => {
			dispatch({
				type: actionTypes.LICENSE_MODELS_LIST_LOADED,
				response
			});
		});
	},

	fetchFinalizedLicenseModels(dispatch) {
		return fetchFinalizedLicenseModels().then(response => dispatch({
			type: actionTypes.FINALIZED_LICENSE_MODELS_LIST_LOADED,
			response
		}));

	},

	fetchLicenseModelById(dispatch, {licenseModelId, version}) {
		
		return fetchLicenseModelById(licenseModelId, version).then(response => {		
			dispatch({
				type: actionTypes.LICENSE_MODEL_LOADED,
				response: {...response, version}
			});
		});
	},

	addLicenseModel(dispatch, {licenseModel}){
		dispatch({
			type: actionTypes.ADD_LICENSE_MODEL,
			licenseModel
		});
	},

	fetchLicenseModelItems(dispatch, {licenseModelId, version}) {
		return Promise.all([
			LicenseAgreementActionHelper.fetchLicenseAgreementList(dispatch, {licenseModelId, version}),
			FeatureGroupsActionHelper.fetchFeatureGroupsList(dispatch, {licenseModelId, version}),
			EntitlementPoolsActionHelper.fetchEntitlementPoolsList(dispatch, {licenseModelId, version}),
			LicenseKeyGroupsActionHelper.fetchLicenseKeyGroupsList(dispatch, {licenseModelId, version})
		]);
	},

	performVCAction(dispatch, {licenseModelId, action, version}) {
		return putLicenseModelAction(licenseModelId, action, version).then(() => {
			if(action === vcActionsEnum.SUBMIT){
				dispatch({
					type: modalActionTypes.GLOBAL_MODAL_SUCCESS,
					data: {
						title: i18n('Submit Succeeded'), 
						msg: i18n('This license model successfully submitted'),
						cancelButtonText: i18n('OK'),						
						timeout: 2000
					}
				});
			}

			let newVersionId = version.id;
			/*
				TODO Temorary switch to change version label
			*/
			switch(action) {
				case vcActionsEnum.CHECK_OUT:
					newVersionId = adjustMinorVersion(version.label, 1);
					break;
				case vcActionsEnum.UNDO_CHECK_OUT:
					newVersionId = adjustMinorVersion(version.label, -1);
					break;
				case vcActionsEnum.SUBMIT:
					newVersionId = adjustMajorVersion(version.label, 1);
			}

			OnboardingActionHelper.updateCurrentScreenVersion(dispatch, {label: newVersionId, id: newVersionId});

			LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version:{id: newVersionId, label: newVersionId}});
			return Promise.resolve({id: newVersionId, label: newVersionId});
		});
	},

	switchVersion(dispatch, {licenseModelId, version}) {		
		LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId, version: {id: version.id, label: version.label}}).then(() => {
			LicenseModelActionHelper.fetchLicenseModelItems(dispatch, {licenseModelId, version});
		});
	},

	saveLicenseModel(dispatch, {licenseModel}) {
		return putLicenseModel(licenseModel).then(() => {
			dispatch({
				type: actionTypes.ADD_LICENSE_MODEL,
				licenseModel
			});
			dispatch({
				type: actionTypes.LICENSE_MODEL_LOADED,
				response: licenseModel
			});
		});
	}

};

export default LicenseModelActionHelper;
