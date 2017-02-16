/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

import {connect} from 'react-redux';
import {default as OnboardingCatalogView, catalogItemTypes} from './OnboardingCatalogView.jsx';
import OnboardingActionHelper from './OnboardingActionHelper.js';
import LicenseModelCreationActionHelper from './licenseModel/creation/LicenseModelCreationActionHelper.js';
import SoftwareProductCreationActionHelper from './softwareProduct/creation/SoftwareProductCreationActionHelper.js';


const mapStateToProps = ({licenseModelList, softwareProductList, softwareProduct: {softwareProductCreation}, licenseModel: {licenseModelCreation}}) => {

	let modalToShow;

	if(licenseModelCreation.data) {
		modalToShow = catalogItemTypes.LICENSE_MODEL;
	} else if(softwareProductCreation.showModal && softwareProductCreation.data) {
		modalToShow = catalogItemTypes.SOFTWARE_PRODUCT;
	}

	return {
		licenseModelList,
		softwareProductList,
		modalToShow
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onSelectLicenseModel({id: licenseModelId}) {
			OnboardingActionHelper.navigateToLicenseAgreements(dispatch, {licenseModelId});
		},
		onSelectSoftwareProduct(softwareProduct) {
			let {id: softwareProductId, vendorId: licenseModelId, licensingVersion} = softwareProduct;
			OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, licenseModelId, licensingVersion});
		},
		onAddSoftwareProductClick: () => SoftwareProductCreationActionHelper.open(dispatch),
		onAddLicenseModelClick: () => LicenseModelCreationActionHelper.open(dispatch)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(OnboardingCatalogView);
