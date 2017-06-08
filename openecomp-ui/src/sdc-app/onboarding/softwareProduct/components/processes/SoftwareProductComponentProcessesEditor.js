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
import SoftwareProductComponentProcessesActionHelper from './SoftwareProductComponentProcessesActionHelper';
import SoftwareProductComponentProcessesEditorView from './SoftwareProductComponentProcessesEditorView.jsx';
import {SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_FORM} from './SoftwareProductComponentProcessesConstants.js';

export const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductComponents: {componentProcesses = {}}} = softwareProduct;
	let {processesList = [], processesEditor = {}} = componentProcesses;
	let {data, genericFieldInfo, formReady} = processesEditor;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	let previousData;
	const processId = data ? data.id : null;
	if(processId) {
		previousData = processesList.find(process => process.id === processId);
	}

	return {
		data,
		genericFieldInfo,
		previousData,
		isFormValid,
		formReady
	};
};

const mapActionsToProps = (dispatch, {softwareProductId, version, componentId}) => {

	return {
		onDataChanged: (deltaData) => ValidationHelper.dataChanged(dispatch, {deltaData, formName: SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_FORM}),
		onCancel: () => SoftwareProductComponentProcessesActionHelper.closeEditor(dispatch),
		onSubmit: ({previousProcess, process}) => {
			SoftwareProductComponentProcessesActionHelper.closeEditor(dispatch);
			SoftwareProductComponentProcessesActionHelper.saveProcess(dispatch, {softwareProductId, version, previousProcess, componentId, process});
		},
		onValidateForm: () => ValidationHelper.validateForm(dispatch, SOFTWARE_PRODUCT_PROCESS_COMPONENTS_EDITOR_FORM)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductComponentProcessesEditorView);
