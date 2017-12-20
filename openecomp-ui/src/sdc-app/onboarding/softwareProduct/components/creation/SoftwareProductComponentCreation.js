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
import SoftwareProductComponentCreationView from './SoftwareProductComponentCreationView.jsx';
import SoftwareProductComponentsActionHelper from '../SoftwareProductComponentsActionHelper.js';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import {forms} from '../SoftwareProductComponentsConstants.js';

export const mapStateToProps = ({softwareProduct}) => {
	let {softwareProductComponents: {componentEditor: {data, genericFieldInfo, formReady}}} = softwareProduct;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);
	return {
		data,
		genericFieldInfo,
		formReady,
		isFormValid
	};
};


const mapActionsToProps = (dispatch, {softwareProductId, version}) => {
	return {
		onDataChanged: (deltaData) => ValidationHelper.dataChanged(dispatch, {deltaData, formName: forms.CREATE_FORM}),
		//onDataChanged: deltaData => SoftwareProductComponentsActionHelper.componentDataChanged(dispatch, {deltaData}),
		onSubmit: (componentData) => {
			return SoftwareProductComponentsActionHelper.createSoftwareProductComponent(dispatch,
			{softwareProductId, componentData, version});
		},
		onCancel: () => SoftwareProductComponentsActionHelper.closeComponentCreationModal(dispatch),
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};

};

export default connect(mapStateToProps, mapActionsToProps)(SoftwareProductComponentCreationView);
