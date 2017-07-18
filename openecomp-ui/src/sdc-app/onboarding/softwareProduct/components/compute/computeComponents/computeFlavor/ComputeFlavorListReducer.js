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

import {actionTypes} from './ComputeFlavorConstants.js';

export default (state = [], action) => {
	switch (action.type) {
		case actionTypes.COMPUTE_FLAVORS_LIST_LOADED:
			return [...action.response.results];
		case actionTypes.ADD_COMPUTE:
			return [...state, action.compute];
		case actionTypes.COMPUTE_LIST_EDIT:
			const indexForEdit = state.findIndex(({id}) => id === action.compute.id);
			return [...state.slice(0, indexForEdit), action.compute, ...state.slice(indexForEdit + 1)];
		case actionTypes.DELETE_COMPUTE:
			return state.filter(({id}) => id !== action.computeId);
		default:
			return state;
	}
};