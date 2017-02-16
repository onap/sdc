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
import {actionTypes} from './SoftwareProductComponentProcessesConstants.js';

function baseUrl(softwareProductId, componentId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/components/${componentId}/processes`;
}

function fetchProcessesList({softwareProductId, componentId, version}) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, componentId)}${versionQuery}`);
}

function deleteProcess({softwareProductId, componentId, processId}) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, componentId)}/${processId}`);
}

function putProcess({softwareProductId, componentId, process}) {
	return RestAPIUtil.save(`${baseUrl(softwareProductId, componentId)}/${process.id}`, {
		name: process.name,
		description: process.description
	});
}

function postProcess({softwareProductId,componentId, process}) {
	return RestAPIUtil.create(`${baseUrl(softwareProductId, componentId)}`, {
		name: process.name,
		description: process.description
	});
}

function uploadFileToProcess({softwareProductId, processId, componentId, formData}) {
	return RestAPIUtil.create(`${baseUrl(softwareProductId, componentId)}/${processId}/upload`, formData);
}



const SoftwareProductComponentProcessesActionHelper = {
	fetchProcessesList(dispatch, {softwareProductId, componentId, version}) {
		dispatch({
			type: actionTypes.FETCH_SOFTWARE_PRODUCT_COMPONENTS_PROCESSES,
			processesList: []
		});

		return fetchProcessesList({softwareProductId, componentId, version}).then(response => {
			dispatch({
				type: actionTypes.FETCH_SOFTWARE_PRODUCT_COMPONENTS_PROCESSES,
				processesList: response.results
			});
		});
	},

	deleteProcess(dispatch, {process, softwareProductId, componentId}) {
		return deleteProcess({softwareProductId, processId:process.id, componentId}).then(() => {
			dispatch({
				type: actionTypes.DELETE_SOFTWARE_PRODUCT_COMPONENTS_PROCESS,
				processId: process.id
			});
		});

	},

	saveProcess(dispatch, {softwareProductId, componentId, previousProcess, process}) {
		if (previousProcess) {
			return putProcess({softwareProductId,componentId,  process}).then(() => {
				if (process.formData && process.formData.name !== previousProcess.artifactName){
					uploadFileToProcess({softwareProductId, processId: process.id, formData: process.formData, componentId});
				}
				dispatch({
					type: actionTypes.EDIT_SOFTWARE_PRODUCT_COMPONENTS_PROCESS,
					process
				});
			});
		}
		else {
			return postProcess({softwareProductId, componentId, process}).then(response => {
				if (process.formData) {
					uploadFileToProcess({softwareProductId, processId: response.value, formData: process.formData, componentId});
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
	},
	processEditorDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: actionTypes.processEditor.DATA_CHANGED,
			deltaData
		});
	}
};

export default SoftwareProductComponentProcessesActionHelper;
