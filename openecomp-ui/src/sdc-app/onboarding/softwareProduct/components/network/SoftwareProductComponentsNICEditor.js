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
import SoftwareProductComponentsNetworkActionHelper from './SoftwareProductComponentsNetworkActionHelper.js';
import SoftwareProductComponentsNICEditorView from './SoftwareProductComponentsNICEditorView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {forms} from 'sdc-app/onboarding/softwareProduct/components/SoftwareProductComponentsConstants.js';
import {NIC_QUESTIONNAIRE} from 'sdc-app/onboarding/softwareProduct/components/network/SoftwareProductComponentsNetworkConstants.js';
import {onboardingMethod as onboardingMethodTypes} from '../../SoftwareProductConstants.js';

export const mapStateToProps = ({softwareProduct, currentScreen}) => {

	let {softwareProductEditor: {data:currentSoftwareProduct = {},  isValidityData = true}, softwareProductComponents} = softwareProduct;
	let {network: {nicEditor = {}}} = softwareProductComponents;
	let {data, qdata, genericFieldInfo, qgenericFieldInfo, dataMap, formReady} = nicEditor;
	let {props: {isReadOnlyMode}} = currentScreen;
	let {onboardingMethod} = currentSoftwareProduct;
	let protocols = [];
	if(qdata && qdata.protocols && qdata.protocols.protocols && qdata.protocols.protocols.length){
		protocols = qdata.protocols.protocols;
	}
	let {version} = currentSoftwareProduct;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo) &&  ValidationHelper.checkFormValid(qgenericFieldInfo);

	return {
		currentSoftwareProduct,
		isValidityData,
		version,
		data,
		qdata,
		dataMap,
		isFormValid,
		formReady,
		genericFieldInfo,
		qgenericFieldInfo,
		isReadOnlyMode,
		protocols,
		isManual: onboardingMethod === onboardingMethodTypes.MANUAL
	};

};

const mapActionsToProps = (dispatch, {softwareProductId, componentId, version}) => {
	return {
		onDataChanged: (deltaData) => ValidationHelper.dataChanged(dispatch, {deltaData,
			formName: forms.NIC_EDIT_FORM}),
		onSubmit: ({data, qdata}) => SoftwareProductComponentsNetworkActionHelper.saveNICDataAndQuestionnaire(dispatch, {softwareProductId, version, componentId, data, qdata}),
		onCancel: () => SoftwareProductComponentsNetworkActionHelper.closeNICEditor(dispatch),
		onValidateForm: () => ValidationHelper.validateForm(dispatch, forms.NIC_EDIT_FORM),
		onQDataChanged: (deltaData) => ValidationHelper.qDataChanged(dispatch, {deltaData,
			qName: NIC_QUESTIONNAIRE}),
	};
};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductComponentsNICEditorView);
