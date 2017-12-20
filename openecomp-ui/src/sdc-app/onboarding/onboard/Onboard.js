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
import OnboardingCatalogActionHelper from './onboardingCatalog/OnboardingCatalogActionHelper.js';
import OnboardActionHelper from './OnboardActionHelper.js';
import LicenseModelCreationActionHelper from '../licenseModel/creation/LicenseModelCreationActionHelper.js';
import SoftwareProductCreationActionHelper from '../softwareProduct/creation/SoftwareProductCreationActionHelper.js';
import sortByStringProperty from 'nfvo-utils/sortByStringProperty.js';
import ScreensHelper from 'sdc-app/common/helpers/ScreensHelper.js';
import {enums, screenTypes} from 'sdc-app/onboarding/OnboardingConstants.js';


export const mapStateToProps = ({
	onboard: {
		onboardingCatalog,
		activeTab,
		searchValue
	},
	licenseModelList,
	users,
	finalizedLicenseModelList,
	softwareProductList,
	finalizedSoftwareProductList
}) => {

	const fullSoftwareProducts = softwareProductList.filter(vsp =>
		!finalizedSoftwareProductList
			.find(fvsp => fvsp.id === vsp.id)
	).concat(finalizedSoftwareProductList);

	const reduceLicenseModelList = (accum, vlm) => {
		let currentSoftwareProductList = sortByStringProperty(
			fullSoftwareProducts
				.filter(vsp => vsp.vendorId === vlm.id),
			'name'
		);
		accum.push({...vlm, softwareProductList: currentSoftwareProductList});
		return accum;
	};

	licenseModelList = sortByStringProperty(
		licenseModelList
			.reduce(reduceLicenseModelList, []),
		'name'
	);

	finalizedLicenseModelList = sortByStringProperty(
		finalizedLicenseModelList
			.reduce(reduceLicenseModelList, []),
		'name'
	);

	const fullLicenseModelList = licenseModelList.filter(vlm =>
		!finalizedLicenseModelList
			.find(fvlm => fvlm.id === vlm.id)
	).concat(finalizedLicenseModelList);

	let {activeTab: catalogActiveTab, vendorCatalog: {vspOverlay, selectedVendor}} = onboardingCatalog;

	return {
		finalizedLicenseModelList,
		finalizedSoftwareProductList,
		licenseModelList,
		softwareProductList,
		fullLicenseModelList,
		activeTab,
		catalogActiveTab,
		searchValue,
		vspOverlay,
		selectedVendor,
		users: users.usersList
	};

};

const mapActionsToProps = (dispatch) => {

	return {
		onSelectLicenseModel({id: licenseModelId, name}, users) {
			ScreensHelper.loadScreen(dispatch, {
				screen: enums.SCREEN.VERSIONS_PAGE, screenType: screenTypes.LICENSE_MODEL,
				props: {licenseModelId, licenseModel: {name}, usersList: users}
			});
		},
		onSelectSoftwareProduct(softwareProduct, users) {
			let {id: softwareProductId, vendorId: licenseModelId, licensingVersion, name} = softwareProduct;
			ScreensHelper.loadScreen(dispatch, {
				screen: enums.SCREEN.SOFTWARE_PRODUCT_VERSIONS_PAGE, screenType: screenTypes.SOFTWARE_PRODUCT,
				props: {softwareProductId, softwareProduct: {name, vendorId: licenseModelId, licensingVersion}, usersList: users}
			});
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
