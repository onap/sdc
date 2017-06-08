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
import {storeCreator} from 'sdc-app/AppStore.js';
import {OnboardingCatalogStoreFactory} from 'test-utils/factories/onboard/OnboardingCatalogFactories.js';
import {LicenseModelStoreFactory} from 'test-utils/factories/licenseModel/LicenseModelFactories.js';
import OnboardingCatalogActionHelper from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogActionHelper.js';
import {tabsMapping} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';


describe('Onboarding Catalog Module Tests', () => {
	it('should return default state', () => {
		const store = storeCreator();
		const expectedStore = OnboardingCatalogStoreFactory.build();
		expect(store.getState().onboard.onboardingCatalog).toEqual(expectedStore);
	});

	it('should change active tab to All', () => {
		const store = storeCreator();
		const expectedStore = OnboardingCatalogStoreFactory.build({activeTab: tabsMapping.ALL});
		OnboardingCatalogActionHelper.changeActiveTab(store.dispatch, tabsMapping.ALL);
		expect(store.getState().onboard.onboardingCatalog).toEqual(expectedStore);
	});


	it('should change VSP Overlay', () => {
		const vendor = LicenseModelStoreFactory.build();
		const store = storeCreator();
		const expectedStore = OnboardingCatalogStoreFactory.build({vendorCatalog: {vspOverlay: vendor.id}});
		OnboardingCatalogActionHelper.changeVspOverlay(store.dispatch, vendor);
		expect(store.getState().onboard.onboardingCatalog).toEqual(expectedStore);
	});

	it('should close VSP Overlay', () => {
		const vendor = LicenseModelStoreFactory.build();
		const store = storeCreator();
		const expectedStore = OnboardingCatalogStoreFactory.build({vendorCatalog: {vspOverlay: null}});
		OnboardingCatalogActionHelper.changeVspOverlay(store.dispatch, vendor);
		OnboardingCatalogActionHelper.changeVspOverlay(store.dispatch, null);
		expect(store.getState().onboard.onboardingCatalog).toEqual(expectedStore);
	});

	it('should select vendor', () => {
		const vendor = LicenseModelStoreFactory.build();
		const store = storeCreator();
		const expectedStore = OnboardingCatalogStoreFactory.build({vendorCatalog: {selectedVendor: vendor}});
		OnboardingCatalogActionHelper.onVendorSelect(store.dispatch, {vendor});
		expect(store.getState().onboard.onboardingCatalog).toEqual(expectedStore);
	});

});
