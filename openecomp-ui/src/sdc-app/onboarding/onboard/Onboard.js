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
import OnboardView from './OnboardView.jsx';
import OnboardingActionHelper from '../OnboardingActionHelper.js';
import OnboardingCatalogActionHelper from './onboardingCatalog/OnboardingCatalogActionHelper.js';
import OnboardActionHelper from './OnboardActionHelper.js';
import LicenseModelCreationActionHelper from '../licenseModel/creation/LicenseModelCreationActionHelper.js';
import SoftwareProductCreationActionHelper from '../softwareProduct/creation/SoftwareProductCreationActionHelper.js';
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';

export const mapStateToProps = ({
	onboard: {onboardingCatalog, activeTab, searchValue}, licenseModelList, finalizedLicenseModelList, softwareProductList, finalizedSoftwareProductList
}) => {

	const reduceLicenseModelList = (accum, vlm)=> {
		let currentSoftwareProductList = sortByStringProperty(
			finalizedSoftwareProductList
				.filter(vsp => vsp.vendorId === vlm.id),
			'name'
		);
		accum.push({...vlm, softwareProductList: currentSoftwareProductList});
		return accum;
	};

	finalizedLicenseModelList = sortByStringProperty(
		licenseModelList
			.filter(vlm => finalizedLicenseModelList.map(finalVlm => finalVlm.id).includes(vlm.id))
			.reduce(reduceLicenseModelList, []),
		'vendorName'
	);

	finalizedSoftwareProductList = sortByStringProperty(
		softwareProductList
			.filter(vsp => finalizedSoftwareProductList.map(finalVsp => finalVsp.id).includes(vsp.id)),
		'name'
	);


	let {activeTab: catalogActiveTab, vendorCatalog: {vspOverlay, selectedVendor}} = onboardingCatalog;

	return {
		finalizedLicenseModelList,
		finalizedSoftwareProductList,
		licenseModelList,
		softwareProductList,
		activeTab,
		catalogActiveTab,
		searchValue,
		vspOverlay,
		selectedVendor
	};
};

const mapActionsToProps = (dispatch) => {
	return {
		onSelectLicenseModel({id: licenseModelId, version}) {
			OnboardingActionHelper.navigateToLicenseModelOverview(dispatch, {licenseModelId, version});
		},
		onSelectSoftwareProduct(softwareProduct) {
			let {id: softwareProductId, vendorId: licenseModelId, licensingVersion, version} = softwareProduct;
			OnboardingActionHelper.navigateToSoftwareProductLandingPage(dispatch, {softwareProductId, version, licenseModelId, licensingVersion});
		},
		onAddSoftwareProductClick: (vendorId) => SoftwareProductCreationActionHelper.open(dispatch, vendorId),
		onAddLicenseModelClick: () => LicenseModelCreationActionHelper.open(dispatch),
		onVspOverlayChange: (vendor) => OnboardingCatalogActionHelper.changeVspOverlay(dispatch, vendor),
		closeVspOverlay: () => OnboardingCatalogActionHelper.closeVspOverlay(dispatch),
		onCatalogTabClick: (tab) => OnboardingCatalogActionHelper.changeActiveTab(dispatch, tab),
		onTabClick: (tab) => OnboardActionHelper.changeActiveTab(dispatch, tab),
		onSearch: (searchValue) => OnboardActionHelper.changeSearchValue(dispatch, searchValue),
		onVendorSelect: (vendor) => OnboardingCatalogActionHelper.onVendorSelect(dispatch, {vendor}),
		onMigrate: ({softwareProduct}) => OnboardingCatalogActionHelper.onMigrate(dispatch, softwareProduct)
	};
};

export default connect(mapStateToProps, mapActionsToProps)(OnboardView);
