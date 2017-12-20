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
import {connect} from 'react-redux';
import SoftwareProductDeploymentView from './SoftwareProductDeploymentView.jsx';
import SoftwareProductDeploymentActionHelper from './SoftwareProductDeploymentActionHelper.js';
import i18n from 'nfvo-utils/i18n/i18n.js';
import {actionTypes as modalActionTypes} from 'nfvo-components/modal/GlobalModalConstants.js';

export function mapStateToProps({softwareProduct}) {
	let {softwareProductComponents: {componentsList}, softwareProductDeployment: {deploymentFlavors}} = softwareProduct;
	return {
		deploymentFlavors,
		componentsList
	};
}

function mapActionToProps(dispatch, {softwareProductId, version}) {
	let modalClassName = 'deployment-flavor-editor';
	return {
		onAddDeployment: componentsList => SoftwareProductDeploymentActionHelper.openDeploymentFlavorEditor(dispatch, {softwareProductId, modalClassName, componentsList, version}),
		onDeleteDeployment: ({id, model}) => dispatch({
			type: modalActionTypes.GLOBAL_MODAL_WARNING,
			data:{
				msg: i18n('Are you sure you want to delete "{model}"?', {model: model}),
				onConfirmed: () => SoftwareProductDeploymentActionHelper.deleteDeploymentFlavor(dispatch, {softwareProductId, deploymentFlavorId: id, version})
			}
		}),
		onEditDeployment: (deploymentFlavor, componentsList) =>
			SoftwareProductDeploymentActionHelper.fetchDeploymentFlavor({softwareProductId, deploymentFlavorId: deploymentFlavor.id, version}).then(response =>
				SoftwareProductDeploymentActionHelper
					.openDeploymentFlavorEditor(dispatch, {softwareProductId, componentsList, modalClassName, deploymentFlavor: {...response.data, id: response.id}, isEdit: true, version}),
			)
	};
}

export default connect(mapStateToProps, mapActionToProps, null, {withRef: true})(SoftwareProductDeploymentView);
