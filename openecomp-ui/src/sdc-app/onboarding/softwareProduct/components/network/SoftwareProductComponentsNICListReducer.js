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
import {actionTypes} from './SoftwareProductComponentsNetworkConstants.js';

export default (state = [], action) => {
	switch (action.type) {
		case actionTypes.NIC_LIST_UPDATE:
			return [...action.response];
		case actionTypes.NIC_LIST_EDIT:
			const indexForEdit = state.findIndex(nic => nic.id === action.nic.id);
			return [...state.slice(0, indexForEdit), action.nic, ...state.slice(indexForEdit + 1)];
		default:
			return state;
	}
};
