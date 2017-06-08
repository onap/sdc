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
import EntitlementPoolsActionHelper from './EntitlementPoolsActionHelper.js';
import EntitlementPoolsEditorView from './EntitlementPoolsEditorView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

const mapStateToProps = ({licenseModel: {entitlementPool}}) => {


	let {data, genericFieldInfo, formReady} = entitlementPool.entitlementPoolEditor;

	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	let previousData, EPNames = {};
	const entitlementPoolId = data ? data.id : null;
	if(entitlementPoolId) {
		previousData = entitlementPool.entitlementPoolsList.find(entitlementPool => entitlementPool.id === entitlementPoolId);
	}

	const list = entitlementPool.entitlementPoolsList;
	for (let i = 0; i < list.length; i++) {
		EPNames[list[i].name] = list[i].id;
	}

	return {
		data,
		genericFieldInfo,
		previousData,
		isFormValid,
		formReady,
		EPNames
	};
};

const mapActionsToProps = (dispatch, {licenseModelId, version}) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onCancel: () => EntitlementPoolsActionHelper.closeEntitlementPoolsEditor(dispatch),
		onSubmit: ({previousEntitlementPool, entitlementPool}) => {
			EntitlementPoolsActionHelper.closeEntitlementPoolsEditor(dispatch);
			EntitlementPoolsActionHelper.saveEntitlementPool(dispatch, {licenseModelId, previousEntitlementPool, entitlementPool, version});
		},
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(EntitlementPoolsEditorView);
