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
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import SoftwareProductComponentsImageActionHelper from './SoftwareProductComponentsImageActionHelper.js';
import SoftwareProductComponentsImageEditorView from './SoftwareProductComponentsImageEditorView.jsx';
import {onboardingMethod as onboardingMethodTypes} from '../../SoftwareProductConstants.js';
import {forms} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import {IMAGE_QUESTIONNAIRE} from './SoftwareProductComponentsImageConstants.js';

export const mapStateToProps = ({
	softwareProduct,
	currentScreen: {props: {isReadOnlyMode}}
}) => {

	let {softwareProductEditor: {data:currentSoftwareProduct = {},  isValidityData = true}, softwareProductComponents} = softwareProduct;

	let {images: {imageEditor = {}}} = softwareProductComponents;
	let {data, qdata, genericFieldInfo, qgenericFieldInfo, dataMap, formReady} = imageEditor;
	let {version, onboardingMethod} = currentSoftwareProduct;
	let isManual =  onboardingMethod === onboardingMethodTypes.MANUAL;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo) &&  ValidationHelper.checkFormValid(qgenericFieldInfo);

	return {
		version,
		currentSoftwareProduct,
		isValidityData,
		data,
		qdata,
		dataMap,
		isFormValid,
		formReady,
		genericFieldInfo,
		qgenericFieldInfo,
		isReadOnlyMode,
		isManual: isManual
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, componentId, version}) => {
	return {
		onDataChanged: (deltaData) => ValidationHelper.dataChanged(dispatch, {deltaData, formName: forms.IMAGE_EDIT_FORM}),
		onSubmit: ({data, qdata}) => SoftwareProductComponentsImageActionHelper.saveImageDataAndQuestionnaire(dispatch, {softwareProductId, componentId, version, data, qdata}),
		onCancel: () => SoftwareProductComponentsImageActionHelper.closeImageEditor(dispatch),
		onValidateForm: customValidations => {
			ValidationHelper.validateForm(dispatch, forms.IMAGE_EDIT_FORM);
			ValidationHelper.qValidateForm(dispatch, IMAGE_QUESTIONNAIRE, customValidations);
		},
		onQDataChanged: (deltaData, customValidations) => ValidationHelper.qDataChanged(dispatch, {deltaData,
			qName: IMAGE_QUESTIONNAIRE, customValidations}),
	};
};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductComponentsImageEditorView);
