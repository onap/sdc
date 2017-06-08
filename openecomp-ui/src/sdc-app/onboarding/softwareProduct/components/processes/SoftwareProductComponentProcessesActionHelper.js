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
import {actionTypes} from './SoftwareProductComponentProcessesConstants.js';

function baseUrl(softwareProductId, version, componentId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${version.id}/components/${componentId}/processes`;
}

function fetchProcessesList({softwareProductId, version, componentId}) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version, componentId)}`);
}

function deleteProcess({softwareProductId, version, componentId, processId}) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, version, componentId)}/${processId}`);
}

function putProcess({softwareProductId, version, componentId, process}) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version, componentId)}/${process.id}`, {
		name: process.name,
		description: process.description,
		type: process.type === '' ? null : process.type
	});
}

function postProcess({softwareProductId, version, componentId, process}) {
	return RestAPIUtil.post(`${baseUrl(softwareProductId, version, componentId)}`, {
		name: process.name,
		description: process.description,
		type: process.type === '' ? null : process.type
	});
}

function uploadFileToProcess({softwareProductId, version, processId, componentId, formData}) {
	return RestAPIUtil.post(`${baseUrl(softwareProductId, version, componentId)}/${processId}/upload`, formData);
}



const SoftwareProductComponentProcessesActionHelper = {
	fetchProcessesList(dispatch, {softwareProductId, version, componentId}) {
		dispatch({
			type: actionTypes.FETCH_SOFTWARE_PRODUCT_COMPONENTS_PROCESSES,
			processesList: []
		});

		return fetchProcessesList({softwareProductId, version, componentId}).then(response => {
			dispatch({
				type: actionTypes.FETCH_SOFTWARE_PRODUCT_COMPONENTS_PROCESSES,
				processesList: response.results
			});
		});
	},

	deleteProcess(dispatch, {process, softwareProductId, version, componentId}) {
		return deleteProcess({softwareProductId, version, processId:process.id, componentId}).then(() => {
			dispatch({
				type: actionTypes.DELETE_SOFTWARE_PRODUCT_COMPONENTS_PROCESS,
				processId: process.id
			});
		});

	},

	saveProcess(dispatch, {softwareProductId, version, componentId, previousProcess, process}) {
		if (previousProcess) {
			return putProcess({softwareProductId, version, componentId,  process}).then(() => {
				if (process.formData && process.formData.name !== previousProcess.artifactName){
					uploadFileToProcess({softwareProductId, version, processId: process.id, formData: process.formData, componentId});
				}
				dispatch({
					type: actionTypes.EDIT_SOFTWARE_PRODUCT_COMPONENTS_PROCESS,
					process
				});
			});
		}
		else {
			return postProcess({softwareProductId, version, componentId, process}).then(response => {
				if (process.formData) {
					uploadFileToProcess({softwareProductId, version, processId: response.value, formData: process.formData, componentId});
				}
				dispatch({
					type: actionTypes.ADD_SOFTWARE_PRODUCT_COMPONENTS_PROCESS,
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
			type: actionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_COMPONENTS_CONFIRM,
			processToDelete: false
		});
	},

	openDeleteProcessesConfirm(dispatch, {process} ) {
		dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_PROCESS_DELETE_COMPONENTS_CONFIRM,
			processToDelete: process
		});
	},

	openEditor(dispatch, process = {}) {
		dispatch({
			type: actionTypes.SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_OPEN,
			process
		});
	},
	closeEditor(dispatch) {
		dispatch({
			type:actionTypes.SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_CLOSE
		});
	}
};

export default SoftwareProductComponentProcessesActionHelper;
