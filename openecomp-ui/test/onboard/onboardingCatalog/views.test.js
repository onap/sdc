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

import React from 'react';
import TestUtils from 'react-addons-test-utils';
import {defaultStoreFactory} from 'test-utils/factories/onboard/OnboardingCatalogFactories.js';
import {FinalizedLicenseModelFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import {VSPEditorFactory} from 'test-utils/factories/softwareProduct/SoftwareProductEditorFactories.js';
import {mapStateToProps} from 'sdc-app/onboarding/onboard/Onboard.js';
import {catalogItemTypes} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';
import OnboardingCatalogView from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogView.jsx';
import VendorItem from 'sdc-app/onboarding/onboard/onboardingCatalog/VendorItem.jsx';
import VSPOverlay from 'sdc-app/onboarding/onboard/onboardingCatalog/VSPOverlay.jsx';
import CatalogItemDetails from 'sdc-app/onboarding/onboard/CatalogItemDetails.jsx';
import DetailsCatalogView from 'sdc-app/onboarding/onboard/DetailsCatalogView.jsx';

describe('OnBoarding Catalog test - View: ', function () {


	it('mapStateToProps mapper exists', () => {
		expect(mapStateToProps).toBeTruthy();
	});

	it('mapStateToProps data test', () => {

		const licenseModelList = FinalizedLicenseModelFactory.buildList(3);
		const softwareProductList = VSPEditorFactory.buildList(4);
		const data = defaultStoreFactory.build({licenseModelList, softwareProductList});

		var results = mapStateToProps(data);
		expect(results.softwareProductList).toBeTruthy();
		expect(results.licenseModelList).toBeTruthy();
		expect(results.activeTab).toBeTruthy();
		expect(results.licenseModelList.length).toEqual(3);
	});

	it('licenseModelList creating algorithm test', () => {

		const finalizedLicenseModelList = FinalizedLicenseModelFactory.buildList(3);
		const licenseModelList = FinalizedLicenseModelFactory.buildList(3);
		const finalizedSoftwareProductList = VSPEditorFactory.buildList(4, {vendorId: finalizedLicenseModelList[0].id});
		const softwareProductList = VSPEditorFactory.buildList(4, {vendorId: finalizedLicenseModelList[1].id});
		const data = defaultStoreFactory.build({licenseModelList, finalizedLicenseModelList, softwareProductList, finalizedSoftwareProductList});

		var results = mapStateToProps(data);
		expect(results.finalizedLicenseModelList[0].softwareProductList.length).toEqual(finalizedSoftwareProductList.length);
	});


	it('Catalog view test', () => {

		const dummyFunc = () => {};
		const licenseModelList = FinalizedLicenseModelFactory.buildList(3);
		const softwareProductList = VSPEditorFactory.buildList(4, {vendorId: licenseModelList[0].id});
		const data = defaultStoreFactory.build({licenseModelList, softwareProductList});

		const func = {
			onAddLicenseModelClick: dummyFunc,
			onAddSoftwareProductClick: dummyFunc,
			closeVspOverlay: dummyFunc,
			onVspOverlayChange: dummyFunc,
			onTabClick: dummyFunc,
			onSearch: dummyFunc,
			onSelectLicenseModel: dummyFunc,
			onSelectSoftwareProduct: dummyFunc,
			resetOnboardingCatalogStore: ''
		};

		let params = {...func, ...mapStateToProps(data)};
		let CatalogView = TestUtils.renderIntoDocument(<OnboardingCatalogView
			{...params}/>);
		expect(CatalogView).toBeTruthy();
	});

	it('VendorItem view test', () => {
		let vendor = FinalizedLicenseModelFactory.build();
		const dummyFunc = () => {};
		let params = {
			softwareProductList: VSPEditorFactory.buildList(4 ,{vendorId: vendor.id}),
			vendor,
			onSelectVSP: dummyFunc,
			shouldShowOverlay: false,
			onVendorSelect: dummyFunc,
			onAddVSP: dummyFunc,
			onVSPIconClick: dummyFunc,
		};

		let VendorItemView = TestUtils.renderIntoDocument(<VendorItem{...params}/>);
		expect(VendorItemView).toBeTruthy();
	});


	it('VSPOverlay view test', () => {

		let params = {
			VSPList: VSPEditorFactory.buildList(10 ,{vendorId: '1'}),
			onSelectVSP: () => {}
		};

		let VSPOverlayView = TestUtils.renderIntoDocument(<div><VSPOverlay {...params}/></div>);
		expect(VSPOverlayView).toBeTruthy();
	});

	it('CatalogItemDetails view test', () => {

		let params = {
			catalogItemData: FinalizedLicenseModelFactory.build(),
			onSelect: () => {},
			catalogItemTypeClass: catalogItemTypes.LICENSE_MODEL
		};

		let CatalogItemDetailsView = TestUtils.renderIntoDocument(<div><CatalogItemDetails {...params}/></div>);
		expect(CatalogItemDetailsView).toBeTruthy();
	});

	it('DetailsCatalogView view test', () => {

		let params = {
			VLMList: FinalizedLicenseModelFactory.buildList(3),
			VSPList:  VSPEditorFactory.buildList(4),
			onSelectVLM: () => {},
			onSelectVSP: () => {},
			onAddVLM: () => {},
			onAddVSP: () => {},
			filter: ''
		};

		let AllCatalog = TestUtils.renderIntoDocument(<DetailsCatalogView {...params}/>);
		expect(AllCatalog).toBeTruthy();
	});
});
