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

import {actionTypes} from './EntitlementPoolsConstants';
export default (state = [], action) => {
	switch (action.type) {
		case actionTypes.ENTITLEMENT_POOLS_LIST_LOADED:
			return [...action.response.results];
		case actionTypes.ADD_ENTITLEMENT_POOL:
			return [...state, action.entitlementPool];
		case actionTypes.EDIT_ENTITLEMENT_POOL:
			const indexForEdit = state.findIndex(entitlementPool => entitlementPool.id === action.entitlementPool.id);
			return [...state.slice(0, indexForEdit), action.entitlementPool, ...state.slice(indexForEdit + 1)];
		case actionTypes.DELETE_ENTITLEMENT_POOL:
			return state.filter(entitlementPool => entitlementPool.id !== action.entitlementPoolId);
		default:
			return state;
	}
};
