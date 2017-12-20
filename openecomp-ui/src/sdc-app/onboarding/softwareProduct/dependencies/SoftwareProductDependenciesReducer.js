
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

import {actionTypes, relationTypes, NEW_RULE_TEMP_ID} from './SoftwareProductDependenciesConstants.js';
import {checkCyclesAndMarkDependencies} from './SoftwareProductDependenciesUtils.js';

let newRowObject = {id: NEW_RULE_TEMP_ID, targetId: null, sourceId: null, relationType: relationTypes.DEPENDS_ON};

export default (state = [Object.assign({}, newRowObject) ], action) => {
	switch (action.type) {
		case actionTypes.SOFTWARE_PRODUCT_DEPENDENCIES_LIST_UPDATE:
			// copying the entity with the data for the row that is in the 'add' mode
			let newDependency = state.find(dependency => dependency.id === NEW_RULE_TEMP_ID);
			action.dependenciesList.push(newDependency);
			// returning list from the server with our 'new entity' row
			return checkCyclesAndMarkDependencies(action.dependenciesList);
		case actionTypes.ADD_SOFTWARE_PRODUCT_DEPENDENCY :
			// resetting the entity with the data for the 'add' mode for a new entity
			let newArray = state.filter(dependency => dependency.id !== NEW_RULE_TEMP_ID);
			newArray.push(Object.assign({}, newRowObject));
			return newArray;
		case actionTypes.UPDATE_NEW_SOFTWARE_PRODUCT_DEPENDENCY :
			// we really only need this for the 'new' row since we need to change the state to get
			// everything updated
			let updateArrayIndex = state.findIndex(dependency => dependency.id === NEW_RULE_TEMP_ID);
			let updateArray = state.slice();
			updateArray.splice(updateArrayIndex, 1, action.item);
			return checkCyclesAndMarkDependencies(updateArray);
		default:
			return state;
	}
};
