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
import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import LicenseModelCreationActionHelper from './LicenseModelCreationActionHelper.js';
import LicenseModelCreationView from './LicenseModelCreationView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import LicenseModelActionHelper from 'sdc-app/onboarding/licenseModel/LicenseModelActionHelper.js';

export const mapStateToProps = ({licenseModelList, licenseModel: {licenseModelCreation}}) => {
	let {genericFieldInfo} = licenseModelCreation;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	let VLMNames = {};
	for (let i = 0; i < licenseModelList.length; i++) {
		VLMNames[licenseModelList[i].vendorName.toLowerCase()] = licenseModelList[i].id;
	}

	return {...licenseModelCreation, isFormValid: isFormValid, VLMNames};
};

export const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onCancel: () => LicenseModelCreationActionHelper.close(dispatch),
		onSubmit: (licenseModel) => {
			LicenseModelCreationActionHelper.close(dispatch);
			LicenseModelCreationActionHelper.createLicenseModel(dispatch, {licenseModel}).then(response => {
				LicenseModelActionHelper.fetchLicenseModels(dispatch).then(() => {
					OnboardingActionHelper.navigateToLicenseModelOverview(dispatch, {licenseModelId: response.value});
				});
			});
		},
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(LicenseModelCreationView);
