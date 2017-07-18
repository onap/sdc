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
import {actionTypes, SP_CREATION_FORM_NAME} from './SoftwareProductCreationConstants.js';


export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.OPEN:
			return {
				...state,
				formName: SP_CREATION_FORM_NAME,
				disableVendor: action.selectedVendorId ? true : false,
				data: {
					vendorId: action.selectedVendorId ? action.selectedVendorId : undefined
				},
				genericFieldInfo: {
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'freeEnglishText', data: true}, {type: 'maxLength', data: 1000}, {type: 'required', data: true}]
					},
					'vendorId' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'subCategory' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'category' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'name' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 25}, {type: 'validateName', data: true}]
					},
					'onboardingMethod' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'requiredChooseOption', data: true}]
					}
				},
				showModal: true
			};
		case actionTypes.RESET_DATA:
			return {};
		default:
			return state;
	}
};
