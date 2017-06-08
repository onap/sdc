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
import {OnboardStoreFactory} from 'test-utils/factories/onboard/OnboardFactories.js';
import OnboardActionHelper from 'sdc-app/onboarding/onboard/OnboardActionHelper.js';
import OnboardingCatalogActionHelper from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogActionHelper.js';
import {tabsMapping as onboardTabsMapping} from 'sdc-app/onboarding/onboard/OnboardConstants.js';
import {tabsMapping as onboardCatalogTabsMapping} from 'sdc-app/onboarding/onboard/onboardingCatalog/OnboardingCatalogConstants.js';

describe('Onboard Module Tests', () => {
	it('should return default state', () => {
		const store = storeCreator();
		const expectedStore = OnboardStoreFactory.build();
		expect(store.getState().onboard).toEqual(expectedStore);
	});

	it('should change active tab to Catalog', () => {
		const store = storeCreator();
		const expectedStore = OnboardStoreFactory.build({activeTab: onboardTabsMapping.CATALOG});
		OnboardActionHelper.changeActiveTab(store.dispatch, onboardTabsMapping.CATALOG);
		expect(store.getState().onboard).toEqual(expectedStore);
	});

	it('should change searchValue', () => {
		const store = storeCreator();
		const expectedStore = OnboardStoreFactory.build({searchValue: 'hello'});
		OnboardActionHelper.changeSearchValue(store.dispatch, 'hello');
		expect(store.getState().onboard).toEqual(expectedStore);
	});

	it('should clear searchValue', () => {
		const store = storeCreator();
		const expectedStore = OnboardStoreFactory.build();
		OnboardActionHelper.changeSearchValue(store.dispatch, 'hello');
		OnboardActionHelper.clearSearchValue(store.dispatch);
		expect(store.getState().onboard).toEqual(expectedStore);
	});

	it('should reset store', () => {
		const store = storeCreator();
		const expectedStore = OnboardStoreFactory.build();
		OnboardActionHelper.changeSearchValue(store.dispatch, 'hello');
		OnboardActionHelper.changeActiveTab(store.dispatch, onboardTabsMapping.CATALOG);
		OnboardingCatalogActionHelper.changeActiveTab(store.dispatch, onboardCatalogTabsMapping.ALL);
		OnboardActionHelper.resetOnboardStore(store.dispatch, 'hello');
		expect(store.getState().onboard).toEqual(expectedStore);
	});

});
