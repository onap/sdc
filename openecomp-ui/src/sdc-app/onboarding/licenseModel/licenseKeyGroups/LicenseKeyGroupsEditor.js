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
import LicenseKeyGroupsActionHelper from './LicenseKeyGroupsActionHelper.js';
import LicenseKeyGroupsEditorView from './LicenseKeyGroupsEditorView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

const mapStateToProps = ({licenseModel: {licenseKeyGroup}}) => {


	let {data, genericFieldInfo, formReady} = licenseKeyGroup.licenseKeyGroupsEditor;

	let previousData, LKGNames = {};
	const licenseKeyGroupId = data ? data.id : null;
	if(licenseKeyGroupId) {
		previousData = licenseKeyGroup.licenseKeyGroupsList.find(licenseKeyGroup => licenseKeyGroup.id === licenseKeyGroupId);
	}

	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	const list = licenseKeyGroup.licenseKeyGroupsList;
	for (let i = 0; i < list.length; i++) {
		LKGNames[list[i].name] = list[i].id;
	}

	return {
		data,
		previousData,
		genericFieldInfo,
		isFormValid,
		formReady,
		LKGNames
	};
};

const mapActionsToProps = (dispatch, {licenseModelId, version}) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onCancel: () => LicenseKeyGroupsActionHelper.closeLicenseKeyGroupEditor(dispatch),
		onSubmit: ({previousLicenseKeyGroup, licenseKeyGroup}) => {
			LicenseKeyGroupsActionHelper.closeLicenseKeyGroupEditor(dispatch);
			LicenseKeyGroupsActionHelper.saveLicenseKeyGroup(dispatch, {licenseModelId, previousLicenseKeyGroup, licenseKeyGroup, version});
		},
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseKeyGroupsEditorView);
