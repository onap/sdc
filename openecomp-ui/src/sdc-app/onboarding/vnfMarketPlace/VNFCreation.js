/*
 * Copyright 2017 Huawei Technologies Co., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
import {connect} from 'react-redux';

/*import OnboardingActionHelper from 'sdc-app/onboarding/OnboardingActionHelper.js';
import SoftwareProductCreationActionHelper from './SoftwareProductCreationActionHelper.js';*/
import VNFCreationView from './VNFCreationView.jsx';
/*import ValidationHelper from 'sdc-app/common/helpers/ValidationHelper.js';*/
import VNFCreationActionHelper  from './VNFCreationActionHelper.js';

export const mapStateToProps = (response/*response{finalizedLicenseModelList, softwareProductList, {softwareProduct: vnfItems }*/) => {
	/*let {genericFieldInfo} = softwareProductCreation;
	let isFormValid = ValidationHelper.checkFormValid(genericFieldInfo);

	let VSPNames = {};
	for (let i = 0; i < softwareProductList.length; i++) {
		VSPNames[softwareProductList[i].name.toLowerCase()] = softwareProductList[i].id;
	}*/

	return {
		/*data: softwareProductCreation.data,
		selectedVendorId: softwareProductCreation.selectedVendorId,
		disableVendor: softwareProductCreation.disableVendor,
		softwareProductCategories,
		finalizedLicenseModelList,
		isFormValid,
		formReady: softwareProductCreation.formReady,
		genericFieldInfo,
		VSPNames*/
		/*vnfItems: [{'name': 'enterprise2DC','version': 'SSAR','vendor': 'huawei','desc': 'zxzx','action':'Download'},
		{'name': 'enterprise1DC','version': 'SSAR1','vendor': 'zte','desc': 'china','action':'Download'},
		{'name': 'enterprise3DC','version': 'SSAR1','vendor': 'huawei','desc': 'china','action':'Download'},
		{'name': 'enterprise2DC','version': 'SSAR3','vendor': 'cisco','desc': 'france','action':'Download'},
		{'name': 'enterprise1DC','version': 'SSAR2','vendor': 'at&t','desc': 'india','action':'Download'}]*/
		vnfItems: response.softwareProduct.vnfCreateReducer ? response.softwareProduct.vnfCreateReducer.vnfItems : []
		
	};
};

export const mapActionsToProps = (dispatch) => {
	return {
		/*onDataChanged: (deltaData, formName, customValidations) => ValidationHelper.dataChanged(dispatch, {deltaData, formName, customValidations}),
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
		onValidateForm: (formName) => ValidationHelper.validateForm(dispatch, formName)*/
		onCancel: () => VNFCreationActionHelper.resetData(dispatch),
		onSubmit: () => {
			console.log('on click of Submit Button');
		}
	};
};

export default connect(mapStateToProps, mapActionsToProps, null, {withRef: true})(VNFCreationView);
