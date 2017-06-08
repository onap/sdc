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
import {actionTypes, tabsMapping} from './OnboardingCatalogConstants.js';
import {combineReducers} from 'redux';
import vendorCatalogReducer from './VendorCatalogReducer.js';

const onboardingCatalogReducer = combineReducers({
	vendorCatalog: vendorCatalogReducer,
	activeTab: (state = tabsMapping.ALL, action) => action.type === actionTypes.CHANGE_ACTIVE_CATALOG_TAB ? action.activeTab : state
});

export default (state, action) => {
	if (action.type === actionTypes.RESET_ONBOARDING_CATALOG_STORE) {
		state = undefined;
	}
	return onboardingCatalogReducer(state, action);
};
