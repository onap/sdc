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
import SoftwareProductCreationActionHelper from './SoftwareProductCreationActionHelper.js';
import SoftwareProductCreationView from './SoftwareProductCreationView.jsx';
import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';
import SoftwareProductActionHelper  from '../SoftwareProductActionHelper.js';

export const mapStateToProps = ({finalizedLicenseModelList, softwareProductList, softwareProduct: {softwareProductCreation, softwareProductCategories} }) => {
	let {genericFieldInfo} = softwareProductCreation;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	let VSPNames = {};
	for (let i = 0; i < softwareProductList.length; i++) {
		VSPNames[softwareProductList[i].name] = softwareProductList[i].id;
	}

	return {
		data: softwareProductCreation.data,
		selectedVendorId: softwareProductCreation.selectedVendorId,
		disableVendor: softwareProductCreation.disableVendor,
		softwareProductCategories,
		finalizedLicenseModelList,
		isFormValid,
		formReady: softwareProductCreation.formReady,
		genericFieldInfo,
		VSPNames
	};
};

export const mapActionsToProps = (dispatch) => {
	return {
		onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
		onCancel: () => SoftwareProductCreationActionHelper.resetData(dispatch),
		onSubmit: (softwareProduct) => {
			SoftwareProductCreationActionHelper.resetData(dispatch);
			SoftwareProductCreationActionHelper.createSoftwareProduct(dispatch, {softwareProduct}).then(response => {
				SoftwareProductActionHelper.fetchSoftwareProductList(dispatch).then(() => {
					let {vendorId: licenseModelId, licensingVersion} = softwareProduct;
					OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId: response.vspId, licenseModelId, licensingVersion});
				});
			});
		},
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(SoftwareProductCreationView);
