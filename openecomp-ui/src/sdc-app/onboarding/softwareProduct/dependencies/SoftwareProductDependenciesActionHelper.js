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
import {actionTypes, NEW_RULE_TEMP_ID} from './SoftwareProductDependenciesConstants.js';

function baseUrl(softwareProductId, version) {
	const versionId = version.id;
	const restPrefix = Configuration.get('restPrefix');
	return `${restPrefix}/v1.0/vendor-software-products/${softwareProductId}/versions/${versionId}/component-dependencies`;
}

function fetchDependencies(softwareProductId, version) {
	return RestAPIUtil.fetch(`${baseUrl(softwareProductId, version)}`);
}

function addDepencency(softwareProductId, version, item) {
	return RestAPIUtil.post(`${baseUrl(softwareProductId, version)}`, {
		sourceId: item.sourceId,
		targetId: item.targetId,
		relationType: item.relationType
	});
}


function updateDepencency(softwareProductId, version, item) {
	return RestAPIUtil.put(`${baseUrl(softwareProductId, version)}/${item.id}`,
		{
			sourceId: item.sourceId,
			targetId: item.targetId,
			relationType: item.relationType
		});
}

function removeDependency(softwareProductId, version, item) {
	return RestAPIUtil.destroy(`${baseUrl(softwareProductId, version)}/${item.id}`);
}


const SoftwareProductDependenciesActionHelper = {
	updateDependency(dispatch, {softwareProductId, version, item}) {
		// if change was made on existing item - we will update the server and refresh the list
		// if change was made on the 'new' row - we will only fire the event
		if (item.id !== NEW_RULE_TEMP_ID) {
			return updateDepencency(softwareProductId, version, item).then(() => {
				return this.fetchDependencies(dispatch, {softwareProductId, version});
			});
		} else {
			dispatch({
				type: actionTypes.UPDATE_NEW_SOFTWARE_PRODUCT_DEPENDENCY,
				item: item
			});
		}
	},

	createDependency(dispatch, {softwareProductId, version, item}) {
		// removing the temp id
		delete item.id;
		// creating the new dependency
		return addDepencency(softwareProductId, version, item).then(() => {
			dispatch({
				type: actionTypes.ADD_SOFTWARE_PRODUCT_DEPENDENCY
			});
			return this.fetchDependencies(dispatch, {softwareProductId, version});
		});
	},

	removeDependency(dispatch, {softwareProductId, version, item}) {
		return removeDependency(softwareProductId, version, item).then( () => {
			return this.fetchDependencies(dispatch, {softwareProductId, version});
		});
	},

	fetchDependencies(dispatch, {softwareProductId, version}) {
		return fetchDependencies(softwareProductId, version).then( response => {
			dispatch({
				type: actionTypes.SOFTWARE_PRODUCT_DEPENDENCIES_LIST_UPDATE,
				dependenciesList : response.results
			});
		});
	}
};

export default SoftwareProductDependenciesActionHelper;
