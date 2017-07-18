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
import {actionTypes, forms} from './SoftwareProductComponentsConstants.js';

export default (state = {}, action) => {
	switch (action.type) {
		case actionTypes.COMPONENT_CREATE_OPEN: 
			return {
				...state,
				formName: forms.CREATE_FORM,
				formReady: null,
				genericFieldInfo: {
					'displayName' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'required', data: true}, {type: 'validateName', data: true}, {type: 'maxLength', data: 25}]
					},
					'description' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 1000}]
					}
				}
			};
		case actionTypes.COMPONENT_LOAD:
			return {
				...state,
				data: action.component,
				formReady: null,
				formName: forms.ALL_SPC_FORMS,
				genericFieldInfo: {
					'displayName' : {
						isValid: true,
						errorText: '',
						validations: []
					},
					'vfcCode' : {
						isValid: true,
						errorText: '',
						validations: []
					},
					'nfcFunction' : {
						isValid: true,
						errorText: '',
						validations: [{type: 'maxLength', data: 30}]
					},
					'description' : {
						isValid: true,
						errorText: '',
						validations: []
					}
				}
			};
		case actionTypes.COMPONENT_UPDATE:
			return {
				...state,
				data: action.component
			};
		case actionTypes.COMPONENT_QUESTIONNAIRE_UPDATE:
			return {
				...state,
				qdata: action.payload.qdata || state.qdata,
				qschema: action.payload.qschema || state.qschema
			};
		case actionTypes.COMPONENT_DATA_CHANGED:
			return {
				...state,
				data: {
					...state.data,
					...action.deltaData
				}
			};
		case actionTypes.COMPONENT_DATA_CLEAR:
			return {};
		default:
			return state;
	}
};
