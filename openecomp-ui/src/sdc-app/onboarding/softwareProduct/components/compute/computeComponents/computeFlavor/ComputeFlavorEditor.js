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
import ComputeFlavorEditorView from './ComputeFlavorEditorView.jsx';
import {COMPUTE_FLAVOR_FORM} from './ComputeFlavorConstants.js';
import ComputeFlavorActionHelper from 'sdc-app/onboarding/softwareProduct/components/compute/ComputeFlavorActionHelper.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {COMPONENTS_COMPUTE_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import {onboardingMethod} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

export const mapStateToProps = ({
	softwareProduct: {
		softwareProductEditor,
		softwareProductComponents: {computeFlavor: {computeEditor = {}}}
	},
	currentScreen: {
		props: {isReadOnlyMode}
	}
}) => {
	const {data: currentSoftwareProduct = {}} = softwareProductEditor;
	let {data , qdata, qgenericFieldInfo, dataMap, genericFieldInfo, formReady} = computeEditor;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	return {
		data,
		qdata,
		qgenericFieldInfo,
		dataMap,
		genericFieldInfo,
		isReadOnlyMode,
		isFormValid,
		formReady,
		isManual: currentSoftwareProduct.onboardingMethod === onboardingMethod.MANUAL
	};
};


const mapActionsToProps = (dispatch, {softwareProductId, componentId, version}) => {
	return {
		onDataChanged: deltaData => ValidationHelper.dataChanged(dispatch, {deltaData, formName: COMPUTE_FLAVOR_FORM}),
		onQDataChanged: deltaData => ValidationHelper.qDataChanged(dispatch, {deltaData, qName: COMPONENTS_COMPUTE_QUESTIONNAIRE}),
		onCancel: () => ComputeFlavorActionHelper.closeComputeEditor(dispatch),
		onSubmit: ({data, qdata}) => ComputeFlavorActionHelper.saveComputeDataAndQuestionnaire(dispatch, {softwareProductId, componentId, data, qdata, version}),
		onValidateForm: () => ValidationHelper.validateForm(dispatch, COMPUTE_FLAVOR_FORM)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(ComputeFlavorEditorView);
