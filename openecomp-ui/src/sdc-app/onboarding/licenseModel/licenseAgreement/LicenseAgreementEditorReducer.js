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

import {actionTypes, defaultState} from './LicenseAgreementConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.licenseAgreementEditor.OPEN:
			return {
				...state,
				data: action.licenseAgreement ? { ...action.licenseAgreement } : defaultState.LICENSE_AGREEMENT_EDITOR_DATA
			};
		case actionTypes.licenseAgreementEditor.DATA_CHANGED:
			return {
				...state,
				data: {
					...state.data,
					...action.deltaData
				}
			};
		case actionTypes.licenseAgreementEditor.CLOSE:
			return {};
		case actionTypes.licenseAgreementEditor.SELECT_TAB:
			return {
				...state,
				selectedTab: action.tab
			};
		case actionTypes.licenseAgreementEditor.SELECT_FEATURE_GROUPS_BUTTONTAB:
			return {
				...state,
				selectedFeatureGroupsButtonTab: action.buttonTab
			};
		default:
			return state;
	}

};
