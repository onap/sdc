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
import {actionTypes} from './FeatureGroupsConstants.js';
export default (state = [], action) => {
	switch (action.type) {
		case actionTypes.FEATURE_GROUPS_LIST_LOADED:
			return [...action.response.results];
		case actionTypes.ADD_FEATURE_GROUPS:
			return [...state, action.featureGroup];
		case actionTypes.EDIT_FEATURE_GROUPS:
			const indexForEdit = state.findIndex(featureGroup => featureGroup.id === action.featureGroup.id);
			return [...state.slice(0, indexForEdit), action.featureGroup, ...state.slice(indexForEdit + 1)];
		case actionTypes.DELETE_FEATURE_GROUPS:
			return state.filter(featureGroup => featureGroup.id !== action.featureGroupId);
		default:
			return state;
	}
};
