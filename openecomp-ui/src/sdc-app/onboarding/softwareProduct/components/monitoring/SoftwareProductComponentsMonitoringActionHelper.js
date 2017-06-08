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
import SoftwareProductComponentsMonitoringConstants, {actionTypes} from './SoftwareProductComponentsMonitoringConstants.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';

const UPLOAD = true;

function baseUrl(vspId, version, componentId) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${vspId}/versions/${versionId}/components/${componentId}/monitors`;
}

function snmpTrapUrl(vspId, version, componentId, isUpload) {
	return `${baseUrl(vspId, version, componentId)}/snmp-trap${isUpload ? '/upload' : ''}`;
}

function snmpPollUrl(vspId, version, componentId, isUpload) {
	return `${baseUrl(vspId, version, componentId)}/snmp${isUpload ? '/upload' : ''}`;
}

let onInvalidFileSizeUpload = (dispatch) => dispatch({
	type: modalActionTypes.GLOBAL_MODAL_ERROR,
	data: {
		title: i18n('Upload Failed'),
		msg: i18n('no zip file was uploaded or zip file doesn\'t exist')
	}
});

let uploadSnmpTrapFile = (dispatch, {softwareProductId, version, componentId, formData}) => {
	RestAPIUtil.post(snmpTrapUrl(softwareProductId, version, componentId, UPLOAD), formData).then(()=> dispatch({
		type: actionTypes.SNMP_TRAP_UPLOADED, data: {filename: formData.get('upload').name}
	}));
};

let uploadSnmpPollFile = (dispatch, {softwareProductId, version, componentId, formData}) => {
	RestAPIUtil.post(snmpPollUrl(softwareProductId, version, componentId, UPLOAD), formData).then(()=> dispatch({
		type: actionTypes.SNMP_POLL_UPLOADED, data: {filename: formData.get('upload').name}
	}));
};

let deleteSnmpTrapFile = (dispatch, {softwareProductId, version, componentId}) => {
	RestAPIUtil.destroy(snmpTrapUrl(softwareProductId, version, componentId, !UPLOAD)).then(()=> dispatch({
		type: actionTypes.SNMP_TRAP_DELETED
	}));
};

let deleteSnmpPollFile = (dispatch, {softwareProductId, version, componentId}) => {
	RestAPIUtil.destroy(snmpPollUrl(softwareProductId, version, componentId, !UPLOAD)).then(()=> dispatch({
		type: actionTypes.SNMP_POLL_DELETED
	}));
};

const SoftwareProductComponentsMonitoringAction = {

	fetchExistingFiles(dispatch, {softwareProductId, version, componentId}){
		RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}/snmp`).then(response =>
			dispatch({
				type: actionTypes.SNMP_FILES_DATA_CHANGE,
				data: {trapFilename: response.snmpTrap, pollFilename: response.snmpPoll}
			})
		);
	},

	uploadSnmpFile(dispatch, {softwareProductId, version, componentId, formData, type}){
		if (formData.get('upload').size) {
			if (type === SoftwareProductComponentsMonitoringConstants.SNMP_TRAP) {
				uploadSnmpTrapFile(dispatch, {softwareProductId, version, componentId, formData});
			}
			else {
				uploadSnmpPollFile(dispatch, {softwareProductId, version, componentId, formData});
			}
		}
		else {
			onInvalidFileSizeUpload(dispatch);
		}
	},

	deleteSnmpFile(dispatch, {softwareProductId, version, componentId, type}){
		if (type === SoftwareProductComponentsMonitoringConstants.SNMP_TRAP) {
			deleteSnmpTrapFile(dispatch, {softwareProductId, version, componentId});
		}
		else {
			deleteSnmpPollFile(dispatch, {softwareProductId, version, componentId});
		}
	}

};

export default SoftwareProductComponentsMonitoringAction;
