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

import {actionTypes} from './SoftwareProductProcessesConstants.js';
import RestAPIUtil from 'nfvo-utils/RestAPIUtil.js';
import Configuration from 'sdc-app/config/Configuration.js';

function baseUrl(svpId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${svpId}/processes`;
}

function putProcess(softwareProductId, process) {
	return RestAPIUtil.save(`${baseUrl(softwareProductId)}/${process.id}`, {
		name: process.name,
		description: process.description
	});
}

function postProcess(softwareProductId, process) {
	return RestAPIUtil.create(`${baseUrl(softwareProductId)}`, {
		name: process.name,
		description: process.description
	});
}

function deleteProcess(softwareProductId, processId) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId)}/${processId}`);
}

function uploadFileToProcess(softwareProductId, processId, formData)
{
	return RestAPIUtil.create(`${baseUrl(softwareProductId)}/${processId}/upload`, formData);
}

function fetchProcesses(softwareProductId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId)}${versionQuery}`);
}



const SoftwareProductActionHelper = {

	fetchProcessesList(dispatch, {softwareProductId, version}) {

		dispatch({
			type: actionTypes.FETCH_SOFTWARE_PRODUCT_PROCESSES,
			processesList: []
		});

		return fetchProcesses(softwareProductId, version).then(response => {
			dispatch({
				type: actionTypes.FETCH_SOFTWARE_PRODUCT_PROCESSES,
				processesList: response.results
			});
		});
	},
	openEditor(dispatch, process = {}) {
		dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_PROCESS_EDITOR_OPEN,
			process
		});
	},

	deleteProcess(dispatch, {process, softwareProductId}) {
		return deleteProcess(softwareProductId, process.id).then(() => {
			dispatch({
				type: actionTypes.DELETE_SOFTWARE_PRODUCT_PROCESS,
				processId: process.id
			});
		});

	},

	closeEditor(dispatch) {
		dispatch({
			type:actionTypes.SOFTWARE_PRODUCT_PROCESS_EDITOR_CLOSE
		});
	},

	processEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: actionTypes.processEditor.DATA_CHANGED,
			deltaData
		});
	},

	saveProcess(dispatch, {softwareProductId, previousProcess, process}) {
		if (previousProcess) {
			return putProcess(softwareProductId, process).then(() => {
				if (process.formData){
					uploadFileToProcess(softwareProductId, process.id, process.formData);
				}
				dispatch({
					type: actionTypes.EDIT_SOFTWARE_PRODUCT_PROCESS,
					process
				});
			});
		}
		else {
			return postProcess(softwareProductId, process).then(response => {
				if (process.formData) {
					uploadFileToProcess(softwareProductId, response.value, process.formData);
				}
				dispatch({
					type: actionTypes.ADD_SOFTWARE_PRODUCT_PROCESS,
					process: {
						...process,
						id: response.value
					}
				});
			});
		}
	},

	hideDeleteConfirm(dispatch) {
		dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_CONFIRM,
			processToDelete: false
		});
	},

	openDeleteProcessesConfirm(dispatch, {process} ) {
		dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_CONFIRM,
			processToDelete: process
		});
	}

};

export default SoftwareProductActionHelper;

