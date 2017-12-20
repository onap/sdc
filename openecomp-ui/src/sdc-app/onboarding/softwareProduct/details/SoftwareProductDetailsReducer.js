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
import {actionTypes, forms} from 'sdc-app/onboarding/softwareProduct/SoftwareProductConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.softwareProductEditor.IS_VALIDITY_DATA_CHANGED:
			return {
				...state,
				isValidityData: action.isValidityData
			};
		case actionTypes.SOFTWARE_PRODUCT_LOADED:
			return {
				...state,
				formName: forms.VENDOR_SOFTWARE_PRODUCT_DETAILS,
				genericFieldInfo: {
					'name' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'validateName', data: true}, {type: 'maxLength', data: 25}, {type: 'required', data: true}]
					},
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					}
				},
				data: action.response
			};
		case actionTypes.TOGGLE_NAVIGATION_ITEM:
			return {
				...state,
				mapOfExpandedIds: action.mapOfExpandedIds
			};
		case actionTypes.LOAD_LICENSING_VERSIONS_LIST:
			return {
				...state,
				licensingVersionsList: action.licensingVersionsList
			};
		default:
			return state;
	}
};
