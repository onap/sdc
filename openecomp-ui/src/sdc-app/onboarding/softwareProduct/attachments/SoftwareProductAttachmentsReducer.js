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

import {actionTypes as softwareProductsActionTypes} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';
import {actionTypes} from './SoftwareProductAttachmentsConstants.js';

const mapVolumeData = ({fileName, env, errors}) => ({
	name: fileName,
	expanded: true,
	type: 'volume',
	children: env && [{
		name: env.fileName,
		errors: env.errors,
		type: 'env'
	}],
	errors
});

const mapNetworkData = ({fileName, env, errors}) => ({
	name: fileName,
	expanded: true,
	type: 'network',
	children: env && [{
		name: env.fileName,
		errors: env.errors,
		type: 'env'
	}],
	errors
});

const mapArtifactsData = ({fileName, errors}) => ({
	name: fileName,
	type: 'artifact',
	errors
});

const mapOtherData = ({fileName, errors}) => ({
	name: fileName,
	type: 'other',
	errors
});


const mapHeatData = ({fileName, env, nested, volume, network, artifacts, errors, other}) => ({
	name: fileName,
	expanded: true,
	type: 'heat',
	errors,
	children: [
		...(volume ? volume.map(mapVolumeData) : []),
		...(network ? network.map(mapNetworkData) : []),
		...(env ? [{
			name: env.fileName,
			errors: env.errors,
			type: 'env'
		}] : []),
		...(artifacts ? artifacts.map(mapArtifactsData) : []),
		...(other ? other.map(mapOtherData) : []),
		...(nested ? nested.map(mapHeatData) : [])
	]
});

function createErrorList(node, parent, deep = 0, errorList = []) {
	if (node.errors) {
		errorList.push(...node.errors.map((error) => ({
			errorLevel: error.level,
			errorMessage: error.message,
			name: node.name,
			hasParent: deep > 2,
			parentName: parent.name,
			type: node.type,
		})));
	}
	if (node.children && node.children.length) {
		node.children.map((child) => createErrorList(child, node, deep + 1, errorList));
	}
	return errorList;
}

const mapValidationDataToTree = validationData => {
	let {HEAT, volume, network, artifacts, other} = validationData.importStructure || {};
	return {
		children: [
			{
				name: 'HEAT',
				expanded: true,
				type: 'heat',
				children: (HEAT ? HEAT.map(mapHeatData) : [])
			},
			...(artifacts ? [{
				name: 'artifacts',
				expanded: true,
				type: 'artifact',
				children: (artifacts ? artifacts.map(mapArtifactsData) : [])
			}] : []),
			...(network ? [{
				name: 'networks',
				expanded: true,
				type: 'network',
				children: (network ? network.map(mapNetworkData) : []),
			}] : []),
			...(volume ? [{
				name: 'volume',
				expanded: true,
				type: 'volume',
				children: (volume ? volume.map(mapVolumeData) : []),
			}] : []),
			...(other ? [{
				name: 'other',
				expanded: true,
				type: 'other',
				children: (other ? other.map(mapOtherData) : []),
			}] : [])
		]
	};
};

const toggleExpanded = (node, path) => {
	let newNode = {...node};
	if (path.length === 0) {
		newNode.expanded = !node.expanded;
	} else {
		let index = path[0];
		newNode.children = [
			...node.children.slice(0, index),
			toggleExpanded(node.children[index], path.slice(1)),
			...node.children.slice(index + 1)
		];
	}
	return newNode;
};

const expandSelected = (node, selectedNode) => {
	let shouldExpand = node.name === selectedNode;
	let children = node.children && node.children.map(child => {
		let {shouldExpand: shouldExpandChild, node: newChild} = expandSelected(child, selectedNode);
		shouldExpand = shouldExpand || shouldExpandChild;
		return newChild;
	});

	return {
		node: {
			...node,
			expanded: node.expanded || shouldExpand,
			children
		},
		shouldExpand
	};
};

export default (state = {attachmentsTree: {}}, action) => {
	switch (action.type) {
		case softwareProductsActionTypes.SOFTWARE_PRODUCT_LOADED:
			let currentSoftwareProduct = action.response;
			let attachmentsTree = currentSoftwareProduct.validationData ? mapValidationDataToTree(currentSoftwareProduct.validationData) : {};
			let errorList = createErrorList(attachmentsTree);
			return {
				...state,
				attachmentsTree,
				errorList
			};
		case actionTypes.TOGGLE_EXPANDED:
			return {
				...state,
				attachmentsTree: toggleExpanded(state.attachmentsTree, action.path)
			};
		case actionTypes.SELECTED_NODE:
			let selectedNode = action.nodeName;
			return {
				...state,
				attachmentsTree: expandSelected(state.attachmentsTree, selectedNode).node,
				selectedNode
			};
		case actionTypes.UNSELECTED_NODE:
			return {
				...state,
				selectedNode: undefined
			};
		default:
			return state;
	}
};
