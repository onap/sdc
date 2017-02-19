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

import {actionTypes} from './FeatureGroupsConstants.js';



export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.featureGroupsEditor.OPEN:
			return {
				...state,
				data: action.featureGroup || {}
			};
		case actionTypes.featureGroupsEditor.DATA_CHANGED:
			return {
				...state,
				data: {
					...state.data,
					...action.deltaData
				}
			};
		case actionTypes.featureGroupsEditor.CLOSE:
			return {};
		case actionTypes.featureGroupsEditor.SELECT_TAB:
			return {
				...state,
				selectedTab: action.tab
			};

		case actionTypes.featureGroupsEditor.SELECTED_ENTITLEMENT_POOLS_BUTTONTAB:
			return {
				...state,
				selectedEntitlementPoolsButtonTab: action.buttonTab
			};
		case actionTypes.featureGroupsEditor.SELECTED_LICENSE_KEY_GROUPS_BUTTONTAB:
			return {
				...state,
				selectedLicenseKeyGroupsButtonTab: action.buttonTab
			};
		default:
			return state;
	}

};
