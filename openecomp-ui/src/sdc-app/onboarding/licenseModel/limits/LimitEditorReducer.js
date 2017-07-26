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

import {actionTypes, LIMITS_FORM_NAME, defaultState} from './LimitEditorConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.OPEN:
			return {
				...state,				
				data: action.limitItem ? {...action.limitItem} : defaultState.LIMITS_EDITOR_DATA,
				formReady: null,
				formName: LIMITS_FORM_NAME,
				genericFieldInfo: {
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 1000}]
					},
					'name' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'maxLength', data: 120}]
					},
					'metric' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}]
					},
					'value' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'numeric', data: true}, {type: 'minimum', data: 0}]
					},
					'unit' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'numeric', data: true}]
					},
					'aggregationFunction' : {
						isValid: true,
						errorText: '',
						validations: []
					},
					'time' : {
						isValid: true,
						errorText: '',
						validations: []
					}
				}
			};
		case actionTypes.CLOSE:
			return {};
		default:
			return state;
	}
};