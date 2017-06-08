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
import LicenseAgreementActionHelper from './LicenseAgreementActionHelper.js';
import LicenseAgreementEditorView from './LicenseAgreementEditorView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';

export const mapStateToProps = ({licenseModel: {licenseAgreement, featureGroup}}) => {


	let {data, selectedTab, genericFieldInfo, formReady} = licenseAgreement.licenseAgreementEditor;
	const list = licenseAgreement.licenseAgreementList;
	const LANames = {};

	let previousData;
	const licenseAgreementId = data ? data.id : null;
	if(licenseAgreementId) {
		previousData = licenseAgreement.licenseAgreementList.find(licenseAgreement => licenseAgreement.id === licenseAgreementId);
	}

	for (let i = 0; i < list.length; i++) {
		LANames[list[i].name] = list[i].id;
	}

	const {featureGroupsList = []} = featureGroup;

	let isFormValid = true;
	let invalidTabs = [];
	for (let field in genericFieldInfo) {
		if (!genericFieldInfo[field].isValid) {
			isFormValid = false;
			let tabId = genericFieldInfo[field].tabId;
			if (invalidTabs.indexOf(tabId) === -1) {
				invalidTabs[invalidTabs.length] = genericFieldInfo[field].tabId;
			}
		}
	}

	return {
		data,
		previousData,
		selectedTab,
		featureGroupsList,
		LANames,
		genericFieldInfo,
		isFormValid,
		formReady,
		invalidTabs
	};
};

export const mapActionsToProps = (dispatch, {licenseModelId, version}) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onTabSelect: tab => LicenseAgreementActionHelper.selectLicenseAgreementEditorTab(dispatch, {tab}),
		onCancel: () => LicenseAgreementActionHelper.closeLicenseAgreementEditor(dispatch),
		onSubmit: ({previousLicenseAgreement, licenseAgreement}) => {
			LicenseAgreementActionHelper.closeLicenseAgreementEditor(dispatch);
			LicenseAgreementActionHelper.saveLicenseAgreement(dispatch, {licenseModelId, previousLicenseAgreement, licenseAgreement, version});
		},
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseAgreementEditorView);
