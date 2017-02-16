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
import {actionTypes} from './LicenseModelConstants.js';
import {actionsEnum as vcActionsEnum} from 'nfvo-components/panel/versionController/VersionControllerConstants.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import NotificationConstants from 'nfvo-components/notifications/NotificationConstants.js';

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
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl()}${licenseModelId}${versionQuery}`);
}

function putLicenseModelAction(id, action) {
	return RestAPIUtil.save(`${baseUrl()}${id}/actions`, {action: action});
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
			if(version) {
				response.version = version;
			}
			dispatch({
				type: actionTypes.LICENSE_MODEL_LOADED,
				response
			});
		});
	},

	addLicenseModel(dispatch, {licenseModel}){
		dispatch({
			type: actionTypes.ADD_LICENSE_MODEL,
			licenseModel
		});
	},

	performVCAction(dispatch, {licenseModelId, action}) {
		return putLicenseModelAction(licenseModelId, action).then(() => {
			if(action === vcActionsEnum.SUBMIT){
				dispatch({
					type: NotificationConstants.NOTIFY_SUCCESS,
					data: {title: i18n('Submit Succeeded'), msg: i18n('This license model successfully submitted'), timeout: 2000}
				});
			}
			return LicenseModelActionHelper.fetchLicenseModelById(dispatch, {licenseModelId});
		});
	}
};

export default LicenseModelActionHelper;
