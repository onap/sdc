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
import {actionTypes} from './OnboardConstants.js';

const OnboardActionHelper = {
	resetOnboardStore(dispatch) {
		dispatch({
			type: actionTypes.RESET_ONBOARD_STORE
		});
	},
	changeActiveTab(dispatch, activeTab) {
		this.clearSearchValue(dispatch);
		dispatch({
			type: actionTypes.CHANGE_ACTIVE_ONBOARD_TAB,
			activeTab
		});
	},
	changeSearchValue(dispatch, searchValue) {
		dispatch({
			type: actionTypes.CHANGE_SEARCH_VALUE,
			searchValue
		});
	},
	clearSearchValue(dispatch) {
		dispatch({
			type: actionTypes.CHANGE_SEARCH_VALUE,
			searchValue: ''
		});
	}
};

export default OnboardActionHelper;
