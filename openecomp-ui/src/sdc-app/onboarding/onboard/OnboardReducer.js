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
import {actionTypes, tabsMapping} from './OnboardConstants.js';
import {combineReducers} from 'redux';
import onboardingCatalogReducer from './onboardingCatalog/OnboardingCatalogReducer.js';

const onboardReducer = combineReducers({
	onboardingCatalog: onboardingCatalogReducer,
	activeTab: (state = tabsMapping.WORKSPACE, action) => action.type === actionTypes.CHANGE_ACTIVE_ONBOARD_TAB ? action.activeTab : state,
	searchValue: (state = '', action) => action.type === actionTypes.CHANGE_SEARCH_VALUE ? action.searchValue : state
});

export default (state, action) => {
	if (action.type === actionTypes.RESET_ONBOARD_STORE) {
		state = undefined;
	}
	return onboardReducer(state, action);
};
