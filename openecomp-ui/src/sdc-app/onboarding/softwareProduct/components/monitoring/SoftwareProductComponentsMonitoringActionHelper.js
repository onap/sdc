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
import i18n from 'nfvo-utils/i18n/i18n.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';
import {actionTypes} from './SoftwareProductComponentsMonitoringConstants.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';

function baseUrl(vspId, version, componentId) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${vspId}/versions/${versionId}/components/${componentId}/uploads`;
}

let onInvalidFileSizeUpload = (dispatch) => dispatch({
	type: modalActionTypes.GLOBAL_MODAL_ERROR,
	data: {
		title: i18n('Upload Failed'),
		msg: i18n('no zip file was uploaded or zip file doesn\'t exist')
	}
});

let uploadFile = (dispatch, {softwareProductId, version, componentId, formData, type}) => {
	return RestAPIUtil.post(`${baseUrl(softwareProductId, version, componentId)}/types/${type}`, formData).then(()=> dispatch({
		type: actionTypes.MONITOR_UPLOADED, data: {filename: formData.get('upload').name, type : type}
	}));
};

let deleteFile = (dispatch, {softwareProductId, version, componentId, type}) => {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, version, componentId)}/types/${type}`).then(()=> dispatch({
		type: actionTypes.MONITOR_DELETED,
		data : { type: type}
	}));
};


const SoftwareProductComponentsMonitoringAction = {

	fetchExistingFiles(dispatch, {softwareProductId, version, componentId}){
		return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}`).then(response =>
			dispatch({
				type: actionTypes.MONITOR_FILES_DATA_CHANGE,
				data: response
			})
		);
	},

	uploadFile(dispatch, {softwareProductId, version, componentId, formData, type}){
		if (formData.get('upload').size) {
			return uploadFile(dispatch, {softwareProductId, version, componentId, formData, type});
		}
		else {
			onInvalidFileSizeUpload(dispatch);
		}
	},

	deleteFile(dispatch, {softwareProductId, version, componentId, type}){
		return deleteFile(dispatch, {softwareProductId, version, componentId, type});
	}

};

export default SoftwareProductComponentsMonitoringAction;
