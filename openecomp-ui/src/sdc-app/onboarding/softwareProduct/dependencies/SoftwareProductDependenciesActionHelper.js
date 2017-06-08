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
import {actionTypes} from './SoftwareProductDependenciesConstants.js';
import uuid from 'uuid-js';

function baseUrl(softwareProductId, version) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/component-dependency-model`;
}

function fetchDependency(softwareProductId, version) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}`);
}

function postDependency(softwareProductId, version, dependenciesList) {
	let modifedDependencyList = dependenciesList ? dependenciesList.filter(item => item.sourceId && item.targetId)
	.map(item => ({sourceId: item.sourceId, targetId: item.targetId, relationType: item.relationType})) : [];
	return RestAPIUtil.post(`${baseUrl(softwareProductId, version)}`, {componentDependencyModels:modifedDependencyList});
}

const SoftwareProductDependenciesActionHelper = {
	updateDependencyList(dispatch, {dependenciesList}) {		
		dispatch({type: actionTypes.SOFTWARE_PRODUCT_DEPENDENCIES_LIST_UPDATE, dependenciesList});
	},
	addDependency(dispatch) {
		dispatch({type: actionTypes.ADD_SOFTWARE_PRODUCT_DEPENDENCY});
	},
	fetchDependencies(dispatch, {softwareProductId, version}) {
		return fetchDependency(softwareProductId, version).then( response => {	
			const dependenciesList = response.results ? response.results.map(item => {return {...item, id: uuid.create().toString()};}) : [];									
			dispatch({
				type: actionTypes.SOFTWARE_PRODUCT_DEPENDENCIES_LIST_UPDATE,
				dependenciesList
			});
		});
	},
	saveDependencies(dispatch, {softwareProductId, version, dependenciesList}) {
		return postDependency(softwareProductId, version, dependenciesList);
	}	
};

export default SoftwareProductDependenciesActionHelper;
