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

import {actionTypes} from './SoftwareProductComponentsConstants.js';

function baseUrl(softwareProductId) {
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/components`;
}

function fetchSoftwareProductComponents(softwareProductId, version) {
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId)}${versionQuery}`);
}

function putSoftwareProductComponentQuestionnaire(softwareProductId, vspComponentId, vspComponent) {
	return RestAPIUtil.save(`${baseUrl(softwareProductId)}/${vspComponentId}/questionnaire`, vspComponent);
}

function fetchSoftwareProductComponentQuestionnaire(softwareProductId, vspComponentId, version){
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId)}/${vspComponentId}/questionnaire${versionQuery}`);
}

function fetchSoftwareProductComponent(softwareProductId, vspComponentId, version){
	let versionQuery = version ? `?version=${version}` : '';
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId)}/${vspComponentId}${versionQuery}`);
}

function putSoftwareProductComponent(softwareProductId, vspComponentId, vspComponent) {
	return RestAPIUtil.save(`${baseUrl(softwareProductId)}/${vspComponentId}`, {
		name: vspComponent.name,
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
		});
	},

	componentDataChanged(dispatch, {deltaData}) {
		dispatch({
			type: actionTypes.COMPONENT_DATA_CHANGED,
			deltaData
		});
	},


	updateSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId, componentData, qdata}) {
		return Promise.all([
			SoftwareProductComponentsActionHelper.updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, vspComponentId, qdata}),
			SoftwareProductComponentsActionHelper.updateSoftwareProductComponentData(dispatch, {softwareProductId, vspComponentId, componentData})
		]);
	},

	updateSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, vspComponentId, qdata}) {
		return putSoftwareProductComponentQuestionnaire(softwareProductId, vspComponentId, qdata);
	},

	updateSoftwareProductComponentData(dispatch, {softwareProductId, vspComponentId, componentData}) {
		return putSoftwareProductComponent(softwareProductId, vspComponentId, componentData).then(() => dispatch({
			type: actionTypes.COMPONENTS_LIST_EDIT,
			component: {
				id: vspComponentId,
				...componentData
			}
		}));
	},


	fetchSoftwareProductComponentQuestionnaire(dispatch, {softwareProductId, vspComponentId, version}) {
		return fetchSoftwareProductComponentQuestionnaire(softwareProductId, vspComponentId, version).then(response => {
			dispatch({
				type: actionTypes.COMPONENT_QUESTIONNAIRE_UPDATE,
				payload: {
					qdata: response.data ? JSON.parse(response.data) : {},
					qschema: JSON.parse(response.schema)
				}
			});
		});
	},

	fetchSoftwareProductComponent(dispatch, {softwareProductId, vspComponentId, version}) {
		return fetchSoftwareProductComponent(softwareProductId, vspComponentId, version).then(response => {
			dispatch({
				type: actionTypes.COMPONENT_UPDATE,
				component: response.data
			});
		});
	},

	componentQuestionnaireUpdated(dispatch, {data}) {
		dispatch({
			type: actionTypes.COMPONENT_QUESTIONNAIRE_UPDATE,
			payload: {
				qdata: data
			}
		});
	},
};

export default SoftwareProductComponentsActionHelper;
