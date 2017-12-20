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

import {actionTypes, COMPONENTS_QUESTIONNAIRE} from './SoftwareProductComponentsConstants.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';

function baseUrl(softwareProductId, version) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/components`;
}

function fetchSoftwareProductComponents(softwareProductId, version) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}`);
}

function putSoftwareProductComponentQuestionnaire(softwareProductId, version, vspComponentId, vspComponent) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version)}/${vspComponentId}/questionnaire`, vspComponent);
}

function fetchSoftwareProductComponentQuestionnaire(softwareProductId, version, vspComponentId){
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}/${vspComponentId}/questionnaire`);
}

function fetchSoftwareProductComponent(softwareProductId, version, vspComponentId){
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}/${vspComponentId}`);
}

function putSoftwareProductComponent(softwareProductId, version, vspComponentId, vspComponent) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version)}/${vspComponentId}`, {
		name: vspComponent.name,
		displayName: vspComponent.displayName,
		vfcCode: vspComponent.vfcCode,
		nfcFunction: vspComponent.nfcFunction,
		description: vspComponent.description
	});
}

function deleteSoftwareProductComponent(softwareProductId, componentId, version) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, version)}/${componentId}`,);
}


function postSoftwareProductComponent(softwareProductId, vspComponent, version) {

	return RestAPIUtil.post(`${baseUrl(softwareProductId, version)}`, {
		name: vspComponent.displayName,
		displayName: vspComponent.displayName,
		description: vspComponent.description
	});
}


const SoftwareProductComponentsActionHelper = {
	fetchSoftwareProductComponents(dispatch, {softwareProductId, version}) {
		return fetchSoftwareProductComponents(softwareProductId, version).then(response => {
			dispatch({
				type: actionTypes.COMPONENTS_LIST_UPDATE,
				componentsList: response.results
			});
			return response;
		});
	},

	updateSoftwareProductComponent(dispatch, {softwareProductId, version, vspComponentId, componentData, qdata}) {
		return Promise.all([
			SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, version, vspComponentId, qdata}),
			SoftwareProductComponentsActionHelper.updateSoftwareProductComponentData(dispatch, {softwareProductId, version, vspComponentId, componentData})
		]);
	},

	updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, version, vspComponentId, qdata}) {
		return putSoftwareProductComponentQuestionnaire(softwareProductId, version, vspComponentId, qdata);
	},

	updateSoftwareProductComponentData(dispatch, {softwareProductId, version, vspComponentId, componentData}) {
		return putSoftwareProductComponent(softwareProductId, version, vspComponentId, componentData).then(() => dispatch({
			type: actionTypes.COMPONENTS_LIST_EDIT,
			component: {
				id: vspComponentId,
				...componentData
			}
		}));
	},

	fetchSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, version, vspComponentId}) {
		return fetchSoftwareProductComponentQuestionnaire(softwareProductId, version, vspComponentId).then(response => {
			ValidationHelper.qDataLoaded(dispatch, {qName: COMPONENTS_QUESTIONNAIRE, response: {qdata: response.data ? JSON.parse(response.data) : {},
				qschema: JSON.parse(response.schema)}});
		});
	},

	fetchSoftwareProductComponent(dispatch, {softwareProductId, version, vspComponentId}) {
		return Promise.all([
			fetchSoftwareProductComponent(softwareProductId, version, vspComponentId).then(response => {
				dispatch({
					type: actionTypes.COMPONENT_LOAD,
					component: response.data
				});
				return response;
			}),
			fetchSoftwareProductComponentQuestionnaire(softwareProductId, version, vspComponentId).then(response => {
				ValidationHelper.qDataLoaded(dispatch, {qName: COMPONENTS_QUESTIONNAIRE, response: {qdata: response.data ? JSON.parse(response.data) : {},
					qschema: JSON.parse(response.schema)}});
			})
		]);
	},


	clearComponentsStore(dispatch) {
		dispatch({
			type: actionTypes.COMPONENTS_LIST_UPDATE,
			componentsList: []
		});
	},

	createSoftwareProductComponent(dispatch,{softwareProductId, componentData, version}) {
		SoftwareProductComponentsActionHelper.closeComponentCreationModal(dispatch);
		/* for mock only */

		dispatch({
			type: actionTypes.COMPONENTS_LIST_UPDATE,
			componentsList: [{id: '123', ...componentData}]
		});

		postSoftwareProductComponent(softwareProductId, componentData, version).then(() => {
			SoftwareProductComponentsActionHelper.fetchSoftwareProductComponents(dispatch, {softwareProductId, version});
		});
	},

	clearComponentCreationData(dispatch) {
		dispatch({
			type: actionTypes.COMPONENT_DATA_CLEAR
		});
	},

	closeComponentCreationModal(dispatch) {
		dispatch({
			type: modalActionTypes.GLOBAL_MODAL_CLOSE
		});
		SoftwareProductComponentsActionHelper.clearComponentCreationData(dispatch);
	},

	deleteComponent(dispatch, {softwareProductId, componentId, version}) {
		deleteSoftwareProductComponent(softwareProductId, componentId, version);
		dispatch({
			type: actionTypes.COMPONENT_DELETE,
			componentId: componentId
		});
	},



};

export default SoftwareProductComponentsActionHelper;
