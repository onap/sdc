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
import SoftwareProductDeploymentEditorView from './SoftwareProductDeploymentEditorView.jsx';
import SoftwareProdcutDeploymentActionHelper from '../SoftwareProductDeploymentActionHelper.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {DEPLOYMENT_FLAVORS_FORM_NAME} from '../SoftwareProductDeploymentConstants.js';

export function mapStateToProps({
	licenseModel,
	softwareProduct,
	currentScreen: {props: {isReadOnlyMode}}
}) {
	let {
		softwareProductEditor: {
			data: currentSoftwareProduct = {}
		},
		softwareProductComponents: {
			componentsList,
			computeFlavor: {
				computesList
			}
		},
		softwareProductDeployment: {
			deploymentFlavors,
			deploymentFlavorEditor: {
				data = {},
				genericFieldInfo,
				formReady
			}
		}
	} = softwareProduct;

	let {
			featureGroup: {
				featureGroupsList
			}
	} = licenseModel;

	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);
	let selectedFeatureGroupsIds = currentSoftwareProduct.licensingData ? currentSoftwareProduct.licensingData.featureGroups || [] : [];
	let selectedFeatureGroupsList = featureGroupsList
		.filter(featureGroup => selectedFeatureGroupsIds.includes(featureGroup.id))
		.map(featureGroup => ({value: featureGroup.id, label: featureGroup.name}));

	let DFNames = {};

	deploymentFlavors.map(deployment => {
		DFNames[deployment.model.toLowerCase()] = deployment.id;
	});

	return {
		data,
		selectedFeatureGroupsList,
		genericFieldInfo,
		DFNames,
		isFormValid,
		formReady,
		isReadOnlyMode,
		componentsList,
		computesList,
		isEdit: Boolean(data.id)
	};
}

function mapActionsToProps(dispatch, {softwareProductId, version}) {
	return {
		onDataChanged: (deltaData, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName: DEPLOYMENT_FLAVORS_FORM_NAME, customValidations}),
		onClose: () => SoftwareProdcutDeploymentActionHelper.closeDeploymentFlavorEditor(dispatch),
		onCreate: data =>  SoftwareProdcutDeploymentActionHelper.createDeploymentFlavor(dispatch, {softwareProductId, data, version}),
		onEdit: data =>  SoftwareProdcutDeploymentActionHelper.editDeploymentFlavor(dispatch, {softwareProductId, deploymentFlavorId: data.id, data, version}),
		onValidateForm: () => ValidationHelper.validateForm(dispatch, DEPLOYMENT_FLAVORS_FORM_NAME)
	};
}

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductDeploymentEditorView);
